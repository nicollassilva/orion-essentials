package dev.thewarrior.Managers.Data.Region;

import dev.thewarrior.Managers.Data.Region.Data.RegionData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache de regiões aplicáveis por jogador.
 * Evita recalcular regiões se o jogador permanece na mesma posição.
 */
public class RegionPlayerCache {
    private static final long CACHE_TTL_MS = 250; // Tempo de vida do cache em milissegundos

    private final Map<UUID, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Obtém regiões do cache se ainda válidas.
     * O cache só é válido se a posição for EXATAMENTE a mesma (em blocos inteiros).
     * Isso evita problemas de borda onde o jogador pode sair de uma região
     * andando apenas 1 bloco.
     *
     * @return Lista de regiões ou null se cache inválido
     */
    @Nullable
    public List<RegionData> get(@Nonnull UUID playerId, @Nonnull String worldName, int x, int y, int z) {
        CacheEntry entry = this.cache.get(playerId);

        if (entry == null) return null;

        // Verifica TTL primeiro (mais provável de invalidar)
        if (System.currentTimeMillis() - entry.timestamp > CACHE_TTL_MS) return null;

        // Posição deve ser EXATAMENTE igual para usar cache
        // Qualquer movimento (mesmo 1 bloco) pode cruzar borda de região
        if (x != entry.x || y != entry.y || z != entry.z) return null;

        // Compara mundo
        if (entry.worldNameHash != worldName.hashCode() || !entry.worldName.equals(worldName)) return null;

        return entry.regions;
    }

    /**
     * Armazena regiões no cache.
     */
    public void put(@Nonnull UUID playerId, @Nonnull String worldName, int x, int y, int z, @Nonnull List<RegionData> regions) {
        this.cache.put(playerId, new CacheEntry(worldName, x, y, z, regions));
    }

    /**
     * Invalida o cache de um jogador.
     */
    public void invalidate(@Nonnull UUID playerId) {
        this.cache.remove(playerId);
    }

    /**
     * Invalida todos os caches (usado quando regiões são modificadas).
     */
    public void invalidateAll() {
        this.cache.clear();
    }

    /**
     * Remove entradas antigas do cache (limpeza periódica).
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        this.cache.entrySet().removeIf(entry -> now - entry.getValue().timestamp > CACHE_TTL_MS * 10);
    }

    private static class CacheEntry {
        final String worldName;
        final int worldNameHash;
        final int x, y, z;
        final long timestamp;
        final List<RegionData> regions;

        CacheEntry(String worldName, int x, int y, int z, List<RegionData> regions) {
            this.worldName = worldName;
            this.worldNameHash = worldName.hashCode();
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = System.currentTimeMillis();
            this.regions = Collections.unmodifiableList(regions);
        }
    }
}
