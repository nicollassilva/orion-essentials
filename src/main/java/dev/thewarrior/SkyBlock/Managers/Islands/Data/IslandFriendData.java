package dev.thewarrior.SkyBlock.Managers.Islands.Data;

import dev.thewarrior.SkyBlock.Managers.Islands.Enum.IslandFriendPermission;

import java.util.UUID;

public class IslandFriendData {
    private UUID uuid;
    private String nickname;
    private IslandFriendPermission permission = IslandFriendPermission.DEFAULT;

    public IslandFriendData(UUID uuid, String nickname) {
        this.uuid = uuid;
        this.nickname = nickname;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getNickname() {
        return nickname;
    }

    public IslandFriendPermission getPermission() {
        return permission;
    }

    public void setPermission(IslandFriendPermission permission) {
        this.permission = permission;
    }
}
