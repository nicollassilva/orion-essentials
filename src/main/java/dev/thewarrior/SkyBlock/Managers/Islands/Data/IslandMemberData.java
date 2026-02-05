package dev.thewarrior.SkyBlock.Managers.Islands.Data;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.thewarrior.SkyBlock.Managers.Islands.Enum.IslandMemberPermission;

import java.util.UUID;

public class IslandMemberData {
    private UUID uuid;
    private String nickname;
    private IslandMemberPermission permission = IslandMemberPermission.DEFAULT;
    private long addedAt = System.currentTimeMillis();

    public IslandMemberData(PlayerRef playerRef) {
        this.uuid = playerRef.getUuid();
        this.nickname = playerRef.getUsername();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getNickname() {
        return nickname;
    }

    public IslandMemberPermission getPermission() {
        return permission;
    }

    public void setPermission(IslandMemberPermission permission) {
        this.permission = permission;
    }

    public long getAddedAt() {
        return addedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        IslandMemberData that = (IslandMemberData) obj;

        return uuid.equals(that.uuid);
    }
}
