package dev.thewarrior;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Commands.Broadcast.BroadcastBaseCommand;
import dev.thewarrior.Commands.Discord.DiscordCommand;
import dev.thewarrior.Commands.Discord.SetDiscordCommand;
import dev.thewarrior.Commands.Spawn.SetSpawnCommand;
import dev.thewarrior.Commands.Spawn.SpawnCommand;
import dev.thewarrior.Commands.Tell.TellCommand;
import dev.thewarrior.Commands.Tell.TellOffCommand;
import dev.thewarrior.Commands.Tell.TellOnCommand;
import dev.thewarrior.Commands.Warp.BaseWarpCommand;
import dev.thewarrior.Commands.Warp.DelWarpCommand;
import dev.thewarrior.Commands.Warp.SetWarpCommand;
import dev.thewarrior.Commands.Warp.WarpsCommand;
import dev.thewarrior.Data.PlayerCommandData;
import dev.thewarrior.Handlers.PlayerEventHandler;
import dev.thewarrior.Managers.PluginConfigManager;
import dev.thewarrior.Managers.TeleportManager;
import dev.thewarrior.Managers.WarpManager;
import dev.thewarrior.Systems.TeleportMovementCheckerSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MultiCommands extends JavaPlugin {
    public static ComponentType<EntityStore, PlayerCommandData> PlayerDataComponent;
    public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public PluginConfigManager pluginConfigManager;
    public WarpManager warpManager;
    public TeleportManager teleportManager;

    public MultiCommands(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.pluginConfigManager = new PluginConfigManager(this.getDataDirectory());
        this.warpManager = new WarpManager(this.getDataDirectory());
        this.teleportManager = new TeleportManager(this.pluginConfigManager);
    }

    @Override
    public void start() {
        PlayerDataComponent = getEntityStoreRegistry().registerComponent(PlayerCommandData.class, "PlayerCommandData", PlayerCommandData.CODEC);

        this.registerCommands();
        this.registerSystems();
        this.registerEvents();

        getLogger().atFine().log("========== MULTI COMMANDS PLUGIN STARTED ==========");
    }

    public PluginConfigManager getConfig() {
        return this.pluginConfigManager;
    }

    public void registerCommands() {
        // Discord
        this.getCommandRegistry().registerCommand(new DiscordCommand(this.pluginConfigManager));
        this.getCommandRegistry().registerCommand(new SetDiscordCommand(this.pluginConfigManager));

        // Tell
        this.getCommandRegistry().registerCommand(new TellCommand());
        this.getCommandRegistry().registerCommand(new TellOnCommand());
        this.getCommandRegistry().registerCommand(new TellOffCommand());

        // Warps
        this.getCommandRegistry().registerCommand(new BaseWarpCommand(this.warpManager, this.teleportManager));
        this.getCommandRegistry().registerCommand(new WarpsCommand(this.warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(this.warpManager));
        this.getCommandRegistry().registerCommand(new DelWarpCommand(this.warpManager));

        // Spawn
        this.getCommandRegistry().registerCommand(new SpawnCommand(this.pluginConfigManager, this.teleportManager));
        this.getCommandRegistry().registerCommand(new SetSpawnCommand(this.pluginConfigManager, this.teleportManager));

        // Broadcast
        this.getCommandRegistry().registerCommand(new BroadcastBaseCommand(this.pluginConfigManager));
    }

    public void registerSystems() {
        this.getEntityStoreRegistry().registerSystem(new TeleportMovementCheckerSystem(this.teleportManager));
    }

    public void registerEvents() {
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEventHandler::onPlayerReady);

        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class,
                event -> PlayerEventHandler.onPlayerDisconnect(event, this.teleportManager)
        );
    }
}
