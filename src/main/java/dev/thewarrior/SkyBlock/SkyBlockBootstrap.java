package dev.thewarrior.SkyBlock;

import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import dev.thewarrior.BasePluginModule;
import dev.thewarrior.OrionBootstrap;
import dev.thewarrior.SkyBlock.Commands.GameModeCommand;
import dev.thewarrior.SkyBlock.Commands.Island.IslandBaseCommand;
import dev.thewarrior.SkyBlock.Events.CobblestoneGeneratorHandler;
import dev.thewarrior.SkyBlock.Handlers.SkyBlockPlayerEventHandler;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Managers.SkyBlockSettingsManager;

public class SkyBlockBootstrap extends BasePluginModule {

    private SkyBlockSettingsManager skyBlockSettingsManager;
    private IslandsManager islandsManager;
    private IslandLevelManager islandLevelManager;

    public SkyBlockBootstrap(OrionBootstrap plugin) {
        super(plugin, "SkyBlock");
    }

    public void setup() {
        super.setup();

        this.skyBlockSettingsManager = new SkyBlockSettingsManager(this.getDataDirectory());
        this.islandsManager = new IslandsManager(this.getDataDirectory(), this.skyBlockSettingsManager);
        this.islandLevelManager = new IslandLevelManager(this.getDataDirectory());
    }

    public void start() {
        super.start();

        this.registerCommands();
        this.registerEvents();
    }

    private void registerCommands() {
        this.plugin.getCommandRegistry().registerCommand(new IslandBaseCommand(this.skyBlockSettingsManager, this.islandsManager, this.plugin.getTeleportManager()));
        this.plugin.getCommandRegistry().registerCommand(new GameModeCommand());
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

    public IslandLevelManager getIslandLevelManager() {
        return this.islandLevelManager;
    }
}
