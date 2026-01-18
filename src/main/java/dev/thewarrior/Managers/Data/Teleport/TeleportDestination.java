package dev.thewarrior.Managers.Data.Teleport;

public class TeleportDestination {
    public final String worldName;
    public final double x;
    public final double y;
    public final double z;

    public final float yaw;
    public final float pitch;

    public TeleportDestination(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
