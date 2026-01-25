package dev.thewarrior.Essentials.Managers;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.Data.Teleport.PendingTeleport;
import dev.thewarrior.Essentials.Managers.Data.Teleport.TeleportDestination;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.Utils.TeleportUtil;
import dev.thewarrior.Essentials.i18n.Messages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages delayed teleports with movement cancellation.
 * Players must stand still during the teleport delay or the teleport is canceled.
 * Teleport destinations are stored as data and executed via buffer.run() callback.
 */
public class TeleportManager {
    private static final String BYPASS_PERMISSION = PermissionUtil.getPermission("teleport.bypass");
    private static final double CANCEL_DISTANCE = 2.0;

    private final PluginConfigManager configManager;
    private final RegionManager regionManager;
    private final ConcurrentHashMap<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();

    public TeleportManager(@Nonnull PluginConfigManager configManager, @Nonnull RegionManager regionManager) {
        this.configManager = configManager;
        this.regionManager = regionManager;
    }

    /**
     * Queues a coordinate-based teleport (for homes, warps, spawn).
     */
    public void queueTeleport(
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> entityRef,
            @Nonnull Store<EntityStore> store,
            @Nonnull Vector3d startPosition,
            @Nonnull String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            @Nullable Message successMessage
    ) {
        queueTeleport(playerRef, entityRef, store, startPosition, worldName, x, y, z, yaw, pitch, successMessage, null);
    }

    /**
     * Queues a coordinate-based teleport with an optional success callback.
     */
    public void queueTeleport(
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> entityRef,
            @Nonnull Store<EntityStore> store,
            @Nonnull Vector3d startPosition,
            @Nonnull String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            @Nullable Message successMessage,
            @Nullable Runnable onSuccess
    ) {
        final UUID playerUuid = playerRef.getUuid();
        int delay = this.configManager.getTeleportDelaySeconds();

        if (delay <= 0 || PermissionsModule.get().hasPermission(playerUuid, BYPASS_PERMISSION)) {
            final Message error = TeleportUtil.teleportSafe(store, entityRef, worldName, x, y, z, yaw, pitch);

            if (error != null) {
                playerRef.sendMessage(error.color(Color.RED));
            } else {
                this.regionManager.invalidatePlayerCache(playerUuid);

                if (successMessage != null) playerRef.sendMessage(successMessage.color(Color.GREEN));
                if (onSuccess != null) onSuccess.run();
            }

            return;
        }

        if (pendingTeleports.containsKey(playerUuid)) {
            playerRef.sendMessage(Messages.TELEPORT_ALREADY_PENDING.color(Color.YELLOW));
            return;
        }

        final TeleportDestination destination = new TeleportDestination(worldName, x, y, z, yaw, pitch);
        final PendingTeleport pending = new PendingTeleport(
                playerRef,
                startPosition,
                destination,
                successMessage,
                delay,
                onSuccess
        );

        pendingTeleports.put(playerUuid, pending);

        playerRef.sendMessage(
                Message.raw(String.format(Messages.TELEPORTING, delay)).color(Color.CYAN)
        );
    }

    /**
     * Queues a player-to-player teleport (for TPA).
     */
    public void queueTeleportToPlayer(
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> entityRef,
            @Nonnull Store<EntityStore> store,
            @Nonnull Vector3d startPosition,
            @Nonnull PlayerRef targetPlayer,
            @Nullable Message successMessage
    ) {
        final UUID playerUuid = playerRef.getUuid();
        int delay = configManager.getTeleportDelaySeconds();

        if (delay <= 0 || PermissionsModule.get().hasPermission(playerUuid, BYPASS_PERMISSION)) {
            TeleportUtil.teleportToPlayer(playerRef, targetPlayer);

            if (successMessage != null) playerRef.sendMessage(successMessage);

            return;
        }

        if (pendingTeleports.containsKey(playerUuid)) {
            playerRef.sendMessage(Messages.TELEPORT_ALREADY_PENDING.color(Color.YELLOW));
            return;
        }

        // Create pending teleport with target player UUID
        PendingTeleport pending = new PendingTeleport(
                playerRef,
                startPosition,
                targetPlayer.getUuid(),
                targetPlayer.getUsername(),
                successMessage,
                delay
        );

        pendingTeleports.put(playerUuid, pending);

        playerRef.sendMessage(
                Message.raw(String.format(Messages.TELEPORTING, delay)).color(Color.CYAN)
        );
    }

    /**
     * Ticks pending teleports. Called from the tick system.
     */
    public void tick(
            @Nonnull UUID playerUuid,
            @Nonnull Ref<EntityStore> currentRef,
            @Nonnull Vector3d currentPosition,
            float deltaTime,
            @Nonnull CommandBuffer<EntityStore> buffer
    ) {
        final PendingTeleport pending = this.pendingTeleports.get(playerUuid);

        if (pending == null) return;

        // Check movement
        double distanceSquared = pending.getStartPosition().distanceSquaredTo(currentPosition);
        double maxDistanceSquared = CANCEL_DISTANCE * CANCEL_DISTANCE;

        if (distanceSquared > maxDistanceSquared) {
            cancelTeleport(playerUuid, Messages.TELEPORT_FAILED_PLAYER_MOVED);
            return;
        }

        pending.addElapsedTime(deltaTime);

        if (pending.isReady()) {
            executeTeleport(playerUuid, currentRef, buffer);
        }
    }

    /**
     * Executes a pending teleport using buffer.run() to defer execution.
     */
    private void executeTeleport(
            @Nonnull UUID playerUuid,
            @Nonnull Ref<EntityStore> currentRef,
            @Nonnull CommandBuffer<EntityStore> buffer
    ) {
        final PendingTeleport pending = this.pendingTeleports.remove(playerUuid);

        if (pending == null) return;

        buffer.run(store -> {
            try {
                if (!currentRef.isValid()) {
                    pending.getPlayerRef().sendMessage(Messages.TELEPORT_FAILED_REFERENCE_INVALID.color(Color.RED));
                    return;
                }

                Message error;

                if (pending.isPlayerTeleport()) {
                    error = TeleportUtil.teleportToPlayerByUuid(store, currentRef, pending.getTargetPlayerUuid());
                } else {
                    TeleportDestination dest = pending.getDestination();

                    error = TeleportUtil.teleportSafe(store, currentRef, dest.worldName,
                            dest.x, dest.y, dest.z, dest.yaw, dest.pitch);
                }

                if (error != null) {
                    pending.getPlayerRef().sendMessage(error.color(Color.RED));
                } else {
                    this.regionManager.invalidatePlayerCache(playerUuid);

                    if (pending.getSuccessMessage() != null) pending.getPlayerRef().sendMessage(pending.getSuccessMessage().color(Color.GREEN));
                    if (pending.getOnSuccess() != null) pending.getOnSuccess().run();

                }
            } catch (Exception e) {
                Logger.error("Failed to execute teleport for " + playerUuid + ": " + e.getMessage());
                pending.getPlayerRef().sendMessage(Messages.TELEPORT_FAILED_EXCEPTION.color(Color.RED));
            }
        });
    }

    /**
     * Cancels a pending teleport for a player.
     */
    public void cancelTeleport(@Nonnull UUID playerUuid, @Nullable Message reason) {
        final PendingTeleport pending = pendingTeleports.remove(playerUuid);

        if (pending != null && reason != null) {
            pending.getPlayerRef().sendMessage(reason.color(Color.YELLOW));
        }
    }

    /**
     * Checks if a player has a pending teleport.
     */
    public boolean hasPendingTeleport(@Nonnull UUID playerUuid) {
        return pendingTeleports.containsKey(playerUuid);
    }

    /**
     * Cleans up pending teleport for a player when they disconnect.
     */
    public void onPlayerQuit(@Nonnull UUID playerUuid) {
        this.pendingTeleports.remove(playerUuid);
    }

    /**
     * Shuts down the manager.
     */
    public void shutdown() {
        this.pendingTeleports.clear();
    }
}
