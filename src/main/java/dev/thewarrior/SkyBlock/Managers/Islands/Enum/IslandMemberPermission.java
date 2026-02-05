package dev.thewarrior.SkyBlock.Managers.Islands.Enum;

public enum IslandMemberPermission {
    DEFAULT("default"),
    BUILD("build"),
    CO_OWNER("co_owner")

    ;

    private final String permissionName;

    IslandMemberPermission(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionName() {
        return permissionName;
    }
}
