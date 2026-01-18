package dev.thewarrior.Managers.Data.Teleport;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

public class TpaRequest {
    private final UUID requesterUuid;
    private final String requesterName;
    private final String targetName;
    private ScheduledFuture<?> expirationFuture;

    public TpaRequest(UUID requesterUuid, String requesterName, String targetName) {
        this.requesterUuid = requesterUuid;
        this.requesterName = requesterName;
        this.targetName = targetName;
    }

    public UUID getRequesterUuid() {
        return this.requesterUuid;
    }

    public String getRequesterName() {
        return this.requesterName;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public void setExpirationFuture(ScheduledFuture<?> future) {
        this.expirationFuture = future;
    }

    public void cancel() {
        if (this.expirationFuture != null) {
            this.expirationFuture.cancel(false);
        }
    }
}
