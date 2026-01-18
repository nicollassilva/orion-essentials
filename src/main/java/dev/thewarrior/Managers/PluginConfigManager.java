package dev.thewarrior.Managers;

import dev.thewarrior.Managers.Composition.StorableManager;
import dev.thewarrior.Managers.Data.PluginConfigData;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class PluginConfigManager extends StorableManager<PluginConfigData> {
    public PluginConfigManager(@Nonnull Path dataFolder) {
        super(dataFolder, "config.json", PluginConfigData.class);
    }

    @Override
    protected PluginConfigData createDefaultData() {
        return new PluginConfigData();
    }

    public String getDiscordLink() {
        return this.data.getDiscordLink();
    }

    public int getTeleportDelaySeconds() {
        return this.data.getTeleportDelaySeconds();
    }

    public CompletableFuture<Void> setDiscordLink(String discordLink) {
        this.data.setDiscordLink(discordLink);

        return this.saveConfig();
    }
}
