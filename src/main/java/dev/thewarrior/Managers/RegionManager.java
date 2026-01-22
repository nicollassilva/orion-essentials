package dev.thewarrior.Managers;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.thewarrior.Managers.Composition.StorableManager;
import dev.thewarrior.Managers.Data.Region.Composition.RegionType;
import dev.thewarrior.Managers.Data.Region.Data.RegionArea;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.Data.Region.Factory.RegionAreaFactory;
import dev.thewarrior.Managers.Data.Region.RegionManagerData;
import dev.thewarrior.Managers.Data.Region.RegionPlayerCache;
import dev.thewarrior.Managers.Data.Region.RegionSpatialIndex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RegionManager extends StorableManager<RegionManagerData> {
    private final RegionAreaFactory regionAreaFactory;
    private final RegionSpatialIndex spatialIndex;
    private final RegionPlayerCache playerCache;

    public RegionManager(@Nonnull Path dataFolder) {
        this.spatialIndex = new RegionSpatialIndex();
        this.regionAreaFactory = new RegionAreaFactory();
        this.playerCache = new RegionPlayerCache();

        super(dataFolder, "regions.json", RegionManagerData.class);
    }

    @Override
    protected RegionManagerData createDefaultData() {
        return new RegionManagerData();
    }

    @Override
    protected void onDataLoaded() {
        rebuildSpatialIndex();

        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::cleanupCache, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * Reconstrói o índice espacial com todas as regiões.
     * Deve ser chamado após modificações nas regiões.
     */
    public void rebuildSpatialIndex() {
        this.spatialIndex.rebuild(this.data.getRegions());
        this.playerCache.invalidateAll();
    }

    public boolean hasRegionWithName(final String name) {
        return this.data.hasRegionWithName(name);
    }

    public boolean hasGlobalRegionOnWorld(final String worldName) {
        return this.data.hasGlobalRegionOnWorld(worldName);
    }

    public List<RegionData> getRegions() {
        return this.data.getRegions();
    }

    public RegionData getRegionByName(final String name) {
        return this.data.getRegionByName(name);
    }

    public void deleteRegion(final RegionData regionData) {
        this.data.removeRegion(regionData);
        this.saveConfig();
        rebuildSpatialIndex();
    }

    public void save() {
        this.saveConfig();
    }

    /**
     * Obtém todas as regiões que se aplicam à posição do jogador.
     * Versão otimizada com cache e índice espacial.
     *
     * @param playerId ID do jogador (para cache)
     * @param transform Componente de transformação do jogador
     * @param world Mundo atual
     * @return Lista de regiões aplicáveis, ordenadas por prioridade (maior primeiro)
     */
    @Nonnull
    public List<RegionData> getCachedApplicableRegions(@Nonnull UUID playerId, @Nonnull TransformComponent transform, @Nonnull World world) {
        String worldName = world.getName();
        int x = (int) Math.floor(transform.getPosition().getX());
        int y = (int) Math.floor(transform.getPosition().getY());
        int z = (int) Math.floor(transform.getPosition().getZ());

        final List<RegionData> cached = this.playerCache.get(playerId, worldName, x, y, z);

        if (cached != null) return cached;

        final List<RegionData> result = findApplicableRegions(worldName, x, y, z);

        this.playerCache.put(playerId, worldName, x, y, z, result);

        return result;
    }

    /**
     * Obtém regiões aplicáveis sem cache (para uso único).
     */
    @Nonnull
    public List<RegionData> getApplicableRegions(@Nonnull TransformComponent transform, @Nonnull World world) {
        String worldName = world.getName();
        int x = (int) Math.floor(transform.getPosition().getX());
        int y = (int) Math.floor(transform.getPosition().getY());
        int z = (int) Math.floor(transform.getPosition().getZ());

        return this.findApplicableRegions(worldName, x, y, z);
    }

    @Nonnull
    private List<RegionData> getApplicableRegions(@Nonnull String worldName, int x, int y, int z) {
        return this.findApplicableRegions(worldName, x, y, z);
    }

    /**
     * Busca interna de regiões aplicáveis usando índice espacial.
     * Resultado já vem ordenado por prioridade do índice espacial.
     */
    @Nonnull
    private List<RegionData> findApplicableRegions(@Nonnull String worldName, int x, int y, int z) {
        List<RegionData> candidates = this.spatialIndex.getCandidateRegions(worldName, x, z);

        if (candidates.isEmpty()) {
            return candidates;
        }

        // Se só tem uma região, verifica e retorna
        if (candidates.size() == 1) {
            RegionData region = candidates.getFirst();

            if (containsPosition(region, x, y, z)) {
                return candidates; // Retorna a mesma lista
            }

            return Collections.emptyList();
        }

        List<RegionData> result = null;

        for (RegionData region : candidates) {
            if (!containsPosition(region, x, y, z)) continue;

            if (result == null) {
                result = new ArrayList<>(candidates.size());
            }

            result.add(region);
        }

        return result != null ? result : Collections.emptyList();
    }

    private boolean containsPosition(@Nonnull RegionData region, int x, int y, int z) {
        RegionArea area = region.getArea();

        return switch (region.getType()) {
            case GLOBAL -> true;
            case AREA -> area.containsArea(x, z);
            case CUBOID -> area.containsCuboid(x, y, z);
            default -> false;
        };
    }

    @Nullable
    public RegionData getHighestPriorityRegion(@Nonnull UUID playerId, @Nonnull TransformComponent transform, @Nonnull World world) {
        final List<RegionData> regions = this.getCachedApplicableRegions(playerId, transform, world);

        return regions.isEmpty() ? null : regions.getFirst();
    }

    public boolean isInAnyRegion(@Nonnull TransformComponent transform, @Nonnull World world) {
        String worldName = world.getName();
        int x = (int) Math.floor(transform.getPosition().getX());
        int y = (int) Math.floor(transform.getPosition().getY());
        int z = (int) Math.floor(transform.getPosition().getZ());

        List<RegionData> candidates = this.spatialIndex.getCandidateRegions(worldName, x, z);

        for (RegionData region : candidates) {
            if (containsPosition(region, x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInRegion(@Nonnull String regionName, @Nonnull TransformComponent transform, @Nonnull World world) {
        RegionData region = getRegionByName(regionName);
        if (region == null) return false;

        String worldName = world.getName();
        if (!region.getWorldName().equals(worldName)) return false;

        int x = (int) Math.floor(transform.getPosition().getX());
        int y = (int) Math.floor(transform.getPosition().getY());
        int z = (int) Math.floor(transform.getPosition().getZ());

        return containsPosition(region, x, y, z);
    }

    /**
     * Invalida o cache de um jogador específico.
     * Chamar quando o jogador mudar de mundo ou teleportar.
     */
    public void invalidatePlayerCache(@Nonnull UUID playerId) {
        this.playerCache.invalidate(playerId);
    }

    public void cleanupCache() {
        this.playerCache.cleanup();
    }

    public void updateRegion(final RegionData regionData, String newName, Integer newPriority, Vector3i newMin, Vector3i newMax) {
        if (newName != null && !newName.isEmpty()) {
            regionData.setName(newName);
        }

        if (newPriority != null && !regionData.getType().equals(RegionType.GLOBAL)) {
            regionData.setPriority(newPriority);
        }

        if (newMin != null && newMax != null && !regionData.getType().equals(RegionType.GLOBAL)) {
            final RegionArea newArea = switch (regionData.getType()) {
                case AREA -> this.regionAreaFactory.createArea(newMin.getX(), newMin.getY(), newMin.getZ(), newMax.getX(), newMax.getY(), newMax.getZ());
                case CUBOID -> this.regionAreaFactory.createCuboid(newMin.getX(), newMin.getY(), newMin.getZ(), newMax.getX(), newMax.getY(), newMax.getZ());
                default -> null;
            };

            if (newArea != null) {
                regionData.setArea(newArea);
            }
        }

        this.saveConfig();
        rebuildSpatialIndex();
    }

    public RegionData create(RegionType type, String name, String worldName, Vector3i min, Vector3i max, int priority) throws Exception {
        final RegionArea area = switch (type) {
            case GLOBAL -> this.regionAreaFactory.createGlobal();
            case AREA -> this.regionAreaFactory.createArea(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            case CUBOID ->  this.regionAreaFactory.createCuboid(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            default -> null;
        };

        if(area == null) {
            throw new InvalidObjectException("Invalid region type provided for region creation: " + type);
        }

        if(type.equals(RegionType.GLOBAL)) {
            priority = -1;
        }

        final RegionData data = new RegionData(UUID.randomUUID(), worldName, name, type, priority, area, System.currentTimeMillis());

        this.data.addRegion(data);
        this.saveConfig();
        rebuildSpatialIndex();

        return data;
    }
}
