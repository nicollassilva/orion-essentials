package dev.thewarrior.SkyBlock;

import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import dev.thewarrior.BasePluginModule;
import dev.thewarrior.OrionBootstrap;
import dev.thewarrior.SkyBlock.Commands.IslandBaseCommand;
import dev.thewarrior.SkyBlock.Events.CobblestoneGeneratorHandler;
import dev.thewarrior.SkyBlock.Handlers.SkyBlockPlayerEventHandler;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Managers.SkyBlockSettingsManager;

public class SkyBlockBootstrap extends BasePluginModule {
    private SkyBlockSettingsManager skyBlockSettingsManager;
    private IslandsManager islandsManager;

    public SkyBlockBootstrap(OrionBootstrap plugin) {
        super(plugin, "SkyBlock");
    }

    public void setup() {
        super.setup();

        this.skyBlockSettingsManager = new SkyBlockSettingsManager(this.getDataDirectory());
        this.islandsManager = new IslandsManager(this.getDataDirectory(), this.skyBlockSettingsManager);
    }

    public void start() {
        super.start();

        this.registerCommands();
        this.registerEvents();
    }

    private void registerCommands() {
        this.plugin.getCommandRegistry().registerCommand(new IslandBaseCommand(this.skyBlockSettingsManager, this.islandsManager));
    }

    private void registerEvents() {
        this.plugin.getEntityStoreRegistry().registerSystem(new CobblestoneGeneratorHandler());

        this.plugin.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, event -> SkyBlockPlayerEventHandler.onPlayerAddedToWorld(event, this));
    }

    public IslandsManager getIslandManager() {
        return this.islandsManager;
    }

    public SkyBlockSettingsManager getSkyBlockSettingsManager() {
        return this.skyBlockSettingsManager;
    }
}
