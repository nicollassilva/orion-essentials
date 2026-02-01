package dev.thewarrior.SkyBlock.Managers.Islands;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IslandsData {
    private ConcurrentHashMap<UUID, IslandData> islands = new ConcurrentHashMap<>();

    public void add(IslandData islandData) {
        this.islands.put(islandData.getId(), islandData);
    }

    public IslandData getIslandByWorldName(final String worldName) {
        for (final IslandData island : this.islands.values()) {
            if(island.getWorldName().equalsIgnoreCase(worldName)) {
                return island;
            }
        }

        return null;
    }

    public IslandData getIslandByNameAndOwner(final String islandName, final UUID ownerId) {
        for (final IslandData island : this.islands.values()) {
            if(island.getIslandName().equalsIgnoreCase(islandName) && island.getOwnerId().equals(ownerId)) {
                return island;
            }
        }

        return null;
    }

    public List<IslandData> getIslandsForPlayer(final PlayerRef ref) {
        final List<IslandData> list = new ObjectArrayList<>();

        for (final IslandData island : this.islands.values()) {
            if(island.getOwnerId() == ref.getUuid()) {
                list.add(island);
            }
        }

        return list;
    }

    public int getIslandCountForPlayer(final PlayerRef ref) {
        int count = 0;

        if(UUIDUtil.isEmptyOrNull(ref.getUuid())) {
            return -1;
        }

        for (final IslandData island : this.islands.values()) {
            if(island.getOwnerId().equals(ref.getUuid())) {
                count++;
            }
        }

        return count;
    }
}
