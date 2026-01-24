package dev.thewarrior.Utils;

public class PermissionUtil {
    public static final String PREFIX = "orionessentials.";

    public static String getPermission(String node) {
        return PREFIX + node;
    }
}
