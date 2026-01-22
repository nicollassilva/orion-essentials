package dev.thewarrior.Managers.Data.Region;

import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Índice espacial otimizado para busca rápida de regiões.
 * Utiliza estratégia de chunking e FastUtil para máxima performance.
 */
public class RegionSpatialIndex {
    private static final int CHUNK_SIZE = 32;
    private static final int CHUNK_SHIFT = 5; // log2(32) = 5
    private static final int MAX_CHUNKS_TO_INDEX = 256; // 16x16 chunks

    // Lista vazia imutável para retorno (evita criar objetos)
    private static final List<RegionData> EMPTY_LIST = Collections.emptyList();

    // Índice: worldName -> chunkKey -> List<RegionData>
    // Usa Long2ObjectMap do FastUtil para chaves long (mais eficiente que Map<Long, ...>)
    private final Map<String, Long2ObjectMap<ObjectArrayList<RegionData>>> chunkIndex = new ConcurrentHashMap<>();

    // Regiões globais por mundo (sempre se aplicam)
    private final Map<String, ObjectArrayList<RegionData>> globalRegions = new ConcurrentHashMap<>();

    // Regiões grandes (não indexadas por chunk) por mundo
    private final Map<String, ObjectArrayList<RegionData>> largeRegions = new ConcurrentHashMap<>();

    // Cache de mundos que têm regiões (evita buscas em mundos vazios)
    private final ObjectOpenHashSet<String> worldsWithRegions = new ObjectOpenHashSet<>();

    /**
     * Reconstrói o índice espacial com as regiões fornecidas.
     * As regiões são pre-ordenadas por prioridade para evitar ordenação na busca.
     */
    public void rebuild(@Nonnull List<RegionData> regions) {
        this.chunkIndex.clear();
        this.globalRegions.clear();
        this.largeRegions.clear();
        this.worldsWithRegions.clear();

        // Pre-ordena por prioridade (maior primeiro), depois por tempo de criação (mais novo primeiro)
        ObjectArrayList<RegionData> sortedRegions = new ObjectArrayList<>(regions);
        sortedRegions.sort(Comparator
                .comparingInt(RegionData::getPriority).reversed()
                .thenComparingLong(RegionData::getCreationTime).reversed()
        );

        for (RegionData region : sortedRegions) {
            String worldName = region.getWorldName();
            this.worldsWithRegions.add(worldName);

            // Regiões globais vão para lista separada
            if (region.getArea().isGlobal()) {
                this.globalRegions.computeIfAbsent(worldName, k -> new ObjectArrayList<>()).add(region);
                continue;
            }

            // Calcula número de chunks que a região cobre
            int minChunkX = region.getArea().getMinX() >> CHUNK_SHIFT;
            int maxChunkX = region.getArea().getMaxX() >> CHUNK_SHIFT;
            int minChunkZ = region.getArea().getMinZ() >> CHUNK_SHIFT;
            int maxChunkZ = region.getArea().getMaxZ() >> CHUNK_SHIFT;

            int totalChunks = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);

            if (totalChunks > MAX_CHUNKS_TO_INDEX) {
                // Região muito grande, vai para lista separada
                this.largeRegions.computeIfAbsent(worldName, k -> new ObjectArrayList<>()).add(region);
            } else {
                // Indexa por chunks
                indexRegionByChunks(region, worldName, minChunkX, maxChunkX, minChunkZ, maxChunkZ);
            }
        }
    }

    private void indexRegionByChunks(RegionData region, String worldName, int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {
        Long2ObjectMap<ObjectArrayList<RegionData>> worldChunks = this.chunkIndex.computeIfAbsent(worldName, k -> new Long2ObjectOpenHashMap<>());

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                long chunkKey = getChunkKey(cx, cz);
                worldChunks.computeIfAbsent(chunkKey, k -> new ObjectArrayList<>()).add(region);
            }
        }
    }

    /**
     * Busca regiões que podem conter a posição especificada.
     * Retorna candidatos JÁ ORDENADOS por prioridade (do rebuild).
     */
    @Nonnull
    public List<RegionData> getCandidateRegions(@Nonnull String worldName, int x, int z) {
        // Early exit: mundo sem regiões
        if (!this.worldsWithRegions.contains(worldName)) {
            return EMPTY_LIST;
        }

        ObjectArrayList<RegionData> globals = this.globalRegions.get(worldName);
        ObjectArrayList<RegionData> large = this.largeRegions.get(worldName);
        Long2ObjectMap<ObjectArrayList<RegionData>> worldChunks = this.chunkIndex.get(worldName);

        ObjectArrayList<RegionData> chunkRegions = null;
        if (worldChunks != null) {
            long chunkKey = getChunkKey(x >> CHUNK_SHIFT, z >> CHUNK_SHIFT);
            chunkRegions = worldChunks.get(chunkKey);
        }

        // Calcula tamanho total para pre-alocar
        int totalSize = (globals != null ? globals.size() : 0)
                      + (large != null ? large.size() : 0)
                      + (chunkRegions != null ? chunkRegions.size() : 0);

        if (totalSize == 0) {
            return EMPTY_LIST;
        }

        // Caso especial: só tem regiões globais
        if (totalSize == (globals != null ? globals.size() : 0) && globals != null) {
            return globals; // Retorna diretamente sem copiar
        }

        // Caso especial: só tem regiões do chunk
        if (globals == null && large == null && chunkRegions != null) {
            return chunkRegions; // Retorna diretamente sem copiar
        }

        // Precisa combinar listas - usa ReferenceOpenHashSet (identity comparison, mais rápido)
        ReferenceOpenHashSet<RegionData> seen = new ReferenceOpenHashSet<>(totalSize);
        ObjectArrayList<RegionData> result = new ObjectArrayList<>(totalSize);

        // Adiciona na ordem de prioridade (já pré-ordenado no rebuild)
        if (globals != null) {
            for (RegionData r : globals) {
                if (seen.add(r)) result.add(r);
            }
        }
        if (chunkRegions != null) {
            for (RegionData r : chunkRegions) {
                if (seen.add(r)) result.add(r);
            }
        }
        if (large != null) {
            for (RegionData r : large) {
                if (seen.add(r)) result.add(r);
            }
        }

        return result;
    }

    private static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public void clear() {
        this.chunkIndex.clear();
        this.globalRegions.clear();
        this.largeRegions.clear();
        this.worldsWithRegions.clear();
    }
}
