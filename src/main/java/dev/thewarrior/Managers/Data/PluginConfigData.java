package dev.thewarrior.Managers.Data;

public class PluginConfigData {
    private String discordLink = "";
    private int teleportDelaySeconds = 3;

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
}
