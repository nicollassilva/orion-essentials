package dev.thewarrior.SkyBlock.Managers;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.SkyBlock.SkyBlockSettingsData;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.nio.file.Path;
import java.util.List;

public class SkyBlockSettingsManager extends StorableManager<SkyBlockSettingsData> {
    private String BYPASS_PERMISSION = PermissionUtil.getPermission("skyblock.bypass_island_limit");

    public SkyBlockSettingsManager(Path dataFolder) {
        super(dataFolder, "config.json", SkyBlockSettingsData.class);
    }

    @Override
    protected SkyBlockSettingsData createDefaultData() {
        return new SkyBlockSettingsData();
    }

    public String getNewWorldNameForPlayer(final PlayerRef playerRef, final int worldNumber) {
        if(playerRef == null || !playerRef.isValid()) return null;
        if(worldNumber < 1) return null;

        final String worldPrefix = this.data.getSettings().getWorldPrefix();

        return worldPrefix
                .replace("{uuid}", playerRef.getUuid().toString())
                .replace("{number}", String.valueOf(worldNumber));
    }

    public boolean isPlayerBlockedToCreateIsland(final Player player, List<IslandData> currentIslands) {
        if(player == null || player.wasRemoved()) return true;
        if(player.hasPermission(BYPASS_PERMISSION, false)) return false;

        final Object2ObjectArrayMap<String, Integer> maxIslands = this.data.getSettings().getMaxIslandsPerPlayer();

        int currentLimit = 1;

        for (final String key : maxIslands.keySet()) {
            int permissionLimit = maxIslands.get(key);

            if(permissionLimit < 1) {
                permissionLimit = 1;
            }

            if(player.hasPermission(key, false) && currentLimit < permissionLimit) {
                currentLimit = maxIslands.get(key);
            }
        }

        return currentIslands.size() >= currentLimit;
    }

    public Vector3d getDefaultIslandSpawnLocation() {
        return this.data.getSpawnLocation();
    }

    public Vector3d getDefaultIslandSpawnRotation() {
        return this.data.getSpawnRotation();
    }

    public String getDefaultPrefabName() {
        return this.data.getPrefabName();
    }
}
