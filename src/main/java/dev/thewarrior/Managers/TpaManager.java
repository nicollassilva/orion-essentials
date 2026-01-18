package dev.thewarrior.Managers;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.thewarrior.Managers.Data.Teleport.TpaRequest;
import dev.thewarrior.Utils.Logger;
import dev.thewarrior.i18n.Messages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Manages teleport requests between players.
 * A target player can have multiple pending requests from different players.
 */
public class TpaManager {
    // Map of target player UUID -> Map of requester UUID -> request
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, TpaRequest>> pendingRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final long EXPIRATION_SECONDS = 20;

    /**
     * Creates a teleport request from one player to another.
     * @param requester The player requesting to teleport
     * @param target The player being requested to accept
     * @return true if request was created, false if there's already a pending request from this requester
     */
    public boolean createRequest(@Nonnull PlayerRef requester, @Nonnull PlayerRef target) {
        UUID targetUuid = target.getUuid();
        UUID requesterUuid = requester.getUuid();

        ConcurrentHashMap<UUID, TpaRequest> targetRequests = pendingRequests.computeIfAbsent(
                targetUuid, _ -> new ConcurrentHashMap<>()
        );

        if (targetRequests.containsKey(requesterUuid)) {
            return false;
        }

        TpaRequest request = new TpaRequest(requesterUuid, requester.getUsername(), target.getUsername());
        targetRequests.put(requesterUuid, request);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            expireRequest(targetUuid, requesterUuid);
        }, EXPIRATION_SECONDS, TimeUnit.SECONDS);

        request.setExpirationFuture(future);

        return true;
    }

    /**
     * Accepts a teleport request from a specific player.
     * @param target The player accepting the request
     * @param requesterName The name of the requester
     * @return The TpaRequest if found and valid, null otherwise
     */
    @Nullable
    public TpaRequest acceptRequest(@Nonnull PlayerRef target, @Nonnull String requesterName) {
        UUID targetUuid = target.getUuid();
        ConcurrentHashMap<UUID, TpaRequest> targetRequests = pendingRequests.get(targetUuid);

        if (targetRequests == null || targetRequests.isEmpty()) {
            return null;
        }

        // Find the request by requester name (case-insensitive)
        TpaRequest foundRequest = null;
        UUID foundRequesterUuid = null;

        for (Map.Entry<UUID, TpaRequest> entry : targetRequests.entrySet()) {
            if (entry.getValue().getRequesterName().equalsIgnoreCase(requesterName)) {
                foundRequest = entry.getValue();
                foundRequesterUuid = entry.getKey();
                break;
            }
        }

        if (foundRequest == null) {
            return null;
        }

        targetRequests.remove(foundRequesterUuid);
        foundRequest.cancel();

        if (targetRequests.isEmpty()) {
            pendingRequests.remove(targetUuid);
        }

        return foundRequest;
    }

    /**
     * Expires a request and notifies the requester.
     */
    private void expireRequest(UUID targetUuid, UUID requesterUuid) {
        final ConcurrentHashMap<UUID, TpaRequest> targetRequests = pendingRequests.get(targetUuid);

        if (targetRequests == null) return;

        final TpaRequest request = targetRequests.remove(requesterUuid);

        if (request == null) return;

        if (targetRequests.isEmpty()) {
            pendingRequests.remove(targetUuid);
        }

        final PlayerRef requester = Universe.get().getPlayer(requesterUuid);

        if (requester != null) {
            requester.sendMessage(
                    Message.raw(String.format(Messages.TELEPORT_REQUEST_EXPIRED, request.getTargetName())).color(Color.PINK)
            );
        }
    }

    /**
     * Cleans up all requests involving a player (both as requester and target).
     * Call this when a player disconnects.
     */
    public void onPlayerQuit(@Nonnull UUID playerUuid) {
        // Remove all requests where this player is the target
        ConcurrentHashMap<UUID, TpaRequest> targetRequests = pendingRequests.remove(playerUuid);
        if (targetRequests != null) {
            // Cancel all expiration futures
            for (TpaRequest request : targetRequests.values()) {
                request.cancel();
            }
        }

        // Remove all requests where this player is the requester
        for (ConcurrentHashMap<UUID, TpaRequest> requests : pendingRequests.values()) {
            TpaRequest request = requests.remove(playerUuid);
            if (request != null) {
                request.cancel();
            }
        }

        // Clean up any empty maps
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Shuts down the manager and cancels all pending requests.
     */
    public void shutdown() {
        scheduler.shutdownNow();
        pendingRequests.clear();
    }
}

