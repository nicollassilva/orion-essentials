package dev.thewarrior.Essentials.Managers.Data.Teleport;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PendingTeleport {
    private final PlayerRef playerRef;
    private final Vector3d startPosition;
    private final TeleportDestination destination; // For coordinate teleports
    private final UUID targetPlayerUuid;           // For player teleports
    private final String targetPlayerName;         // For player teleports
    private final Message successMessage;
    private final Runnable onSuccess;
    private final float delaySeconds;
    private float elapsedTime;

    public PendingTeleport(
            @Nonnull PlayerRef playerRef,
            @Nonnull Vector3d startPosition,
            @Nonnull TeleportDestination destination,
            @Nullable Message successMessage,
            int delaySeconds,
            @Nullable Runnable onSuccess
    ) {
        this.playerRef = playerRef;
        this.startPosition = startPosition.clone();
        this.destination = destination;
        this.targetPlayerUuid = null;
        this.targetPlayerName = null;
        this.successMessage = successMessage;
        this.onSuccess = onSuccess;
        this.delaySeconds = delaySeconds;
        this.elapsedTime = 0f;
    }

    public PendingTeleport(
            @Nonnull PlayerRef playerRef,
            @Nonnull Vector3d startPosition,
            @Nonnull UUID targetPlayerUuid,
            @Nonnull String targetPlayerName,
            @Nullable Message successMessage,
            int delaySeconds
    ) {
        this.playerRef = playerRef;
        this.startPosition = startPosition.clone();
        this.destination = null;
        this.targetPlayerUuid = targetPlayerUuid;
        this.targetPlayerName = targetPlayerName;
        this.successMessage = successMessage;
        this.onSuccess = null;
        this.delaySeconds = delaySeconds;
        this.elapsedTime = 0f;
    }

    public PlayerRef getPlayerRef() {
        return playerRef;
    }

    public Vector3d getStartPosition() {
        return startPosition;
    }

    public TeleportDestination getDestination() {
        return destination;
    }

    public UUID getTargetPlayerUuid() {
        return targetPlayerUuid;
    }

    public boolean isPlayerTeleport() {
        return targetPlayerUuid != null;
    }

    public Message getSuccessMessage() {
        return successMessage;
    }

    public Runnable getOnSuccess() {
        return onSuccess;
    }

    public void addElapsedTime(float deltaTime) {
        this.elapsedTime += deltaTime;
    }

    public boolean isReady() {
        return elapsedTime >= delaySeconds;
    }
}
