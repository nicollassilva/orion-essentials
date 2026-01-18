package dev.thewarrior.Managers.Data;

import dev.thewarrior.Utils.Location;

public class PluginConfigData {
    private String discordLink = "";
    private int teleportDelaySeconds = 3;
    private Location spawnLocation = null;

    public String getDiscordLink() {
        return discordLink;
    }

    public void setDiscordLink(String discordLink) {
        this.discordLink = discordLink;
    }

    public int getTeleportDelaySeconds() {
        return teleportDelaySeconds;
    }

    public void setTeleportDelaySeconds(int teleportDelaySeconds) {
        this.teleportDelaySeconds = teleportDelaySeconds;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}
