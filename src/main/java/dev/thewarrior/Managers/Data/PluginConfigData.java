package dev.thewarrior.Managers.Data;

import dev.thewarrior.Managers.Data.Config.PlayerEntryConfig;
import dev.thewarrior.Utils.Location;

public class PluginConfigData {
    private String discordLink = "";
    private int teleportDelaySeconds = 3;
    private Location spawnLocation = null;
    private PlayerEntryConfig playerJoinConfig = new PlayerEntryConfig();
    private String broadcastFormat = "&e[&6Broadcast&e] &f{message}";
    private String chatFormat = "{prefix}{player}{suffix}&r: {message}";

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

    public String getBroadcastFormat() {
        return broadcastFormat;
    }

    public String getChatFormat() {
        return chatFormat;
    }

    public PlayerEntryConfig getPlayerJoinConfig() {
        return playerJoinConfig;
    }
}
