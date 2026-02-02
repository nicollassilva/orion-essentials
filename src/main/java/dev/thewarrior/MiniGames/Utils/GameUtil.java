package dev.thewarrior.MiniGames.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.PluginConfigManager;
import dev.thewarrior.Essentials.Utils.Location;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.Essentials.Utils.TeleportUtil;

public class GameUtil {
    public static int[] calculateBalancedDistribution(final int toBeBalanced, final int baseCount) {
        final int[] distribution = new int[baseCount];

        if (toBeBalanced <= baseCount) {
            // Each player gets their own spawn (1 or 0 per spawn)
            for (int i = 0; i < toBeBalanced; i++) {
                distribution[i] = 1;
            }
        } else {
            // Distribute players evenly across spawns
            final int basePlayersPerSpawn = toBeBalanced / baseCount;
            final int remainder = toBeBalanced % baseCount;

            for (int i = 0; i < baseCount; i++) {
                // First 'remainder' spawns get one extra player
                distribution[i] = basePlayersPerSpawn + (i < remainder ? 1 : 0);
            }
        }

        return distribution;
    }

    public static void teleportPlayerToServerSpawn(final PlayerRef playerRef) {
        final Location spawnLocation = PluginConfigManager.SPAWN_LOCATION;

        if(spawnLocation == null) {
            Logger.warning("Spawn location is not set in the config. Cannot teleport player to spawn.");
            return;
        }

        if(playerRef == null || !playerRef.isValid()) return;

        final Ref<EntityStore> ref = playerRef.getReference();

        if(ref == null || !ref.isValid()) return;

        TeleportUtil.teleport(
                playerRef, ref.getStore(), ref,
                spawnLocation.getWorld(),
                spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(),
                spawnLocation.getYaw(), spawnLocation.getPitch()
        );
    }
}
