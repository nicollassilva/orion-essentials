package dev.thewarrior;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Commands.Broadcast.BroadcastBaseCommand;
import dev.thewarrior.Commands.Camera.FreeCameraCommand;
import dev.thewarrior.Commands.Discord.DiscordCommand;
import dev.thewarrior.Commands.Discord.SetDiscordCommand;
import dev.thewarrior.Commands.Home.DelHomeCommand;
import dev.thewarrior.Commands.Home.HomeCommand;
import dev.thewarrior.Commands.Home.HomesCommand;
import dev.thewarrior.Commands.Home.SetHomeCommand;
import dev.thewarrior.Commands.MultiCommands.PluginReloadCommand;
import dev.thewarrior.Commands.Permissions.PermissionsCommand;
import dev.thewarrior.Commands.Permissions.PermissionsManageCommand;
import dev.thewarrior.Commands.Spawn.SetSpawnCommand;
import dev.thewarrior.Commands.Spawn.SpawnCommand;
import dev.thewarrior.Commands.Teleports.TpHereCommand;
import dev.thewarrior.Commands.Tell.ReplyCommand;
import dev.thewarrior.Commands.Tell.TellCommand;
import dev.thewarrior.Commands.Tell.TellOffCommand;
import dev.thewarrior.Commands.Tell.TellOnCommand;
import dev.thewarrior.Commands.Tpa.*;
import dev.thewarrior.Commands.Warp.BaseWarpCommand;
import dev.thewarrior.Commands.Warp.DelWarpCommand;
import dev.thewarrior.Commands.Warp.SetWarpCommand;
import dev.thewarrior.Commands.Warp.WarpsCommand;
import dev.thewarrior.Components.PlayerCommandComponent;
import dev.thewarrior.Handlers.PlayerEventHandler;
import dev.thewarrior.Managers.PluginConfigManager;
import dev.thewarrior.Managers.TeleportManager;
import dev.thewarrior.Managers.TpaManager;
import dev.thewarrior.Managers.WarpManager;
import dev.thewarrior.Systems.TeleportMovementCheckerSystem;
import dev.thewarrior.Utils.ColorUtil;
import dev.thewarrior.Utils.Logger;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MultiCommands extends JavaPlugin {
    public static ComponentType<EntityStore, PlayerCommandComponent> PlayerDataComponent;
    public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public PluginConfigManager pluginConfigManager;
    public WarpManager warpManager;
    public TeleportManager teleportManager;
    public TpaManager tpaManager;

    public MultiCommands(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Logger.init(getLogger());
        Logger.info("MultiCommands Plugin is setting up...");

        this.pluginConfigManager = new PluginConfigManager(this.getDataDirectory());
        this.warpManager = new WarpManager(this.getDataDirectory());
        this.teleportManager = new TeleportManager(this.pluginConfigManager);
        this.tpaManager = new TpaManager();
    }

    @Override
    public void start() {
        PlayerDataComponent = getEntityStoreRegistry().registerComponent(PlayerCommandComponent.class, "PlayerCommandData", PlayerCommandComponent.CODEC);

        this.registerCommands();
        this.registerSystems();
        this.registerEvents();

        Logger.info("MultiCommands has been started!");
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
        this.getCommandRegistry().registerCommand(new ReplyCommand());

        // Warps
        this.getCommandRegistry().registerCommand(new BaseWarpCommand(this.warpManager, this.teleportManager));
        this.getCommandRegistry().registerCommand(new WarpsCommand(this.warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(this.warpManager));
        this.getCommandRegistry().registerCommand(new DelWarpCommand(this.warpManager));

        // Teleports
        this.getCommandRegistry().registerCommand(new TpHereCommand());

        // Spawn
        this.getCommandRegistry().registerCommand(new SpawnCommand(this.pluginConfigManager, this.teleportManager));
        this.getCommandRegistry().registerCommand(new SetSpawnCommand(this.pluginConfigManager, this.teleportManager));

        // Broadcast
        this.getCommandRegistry().registerCommand(new BroadcastBaseCommand(this.pluginConfigManager));

        // Home
        this.getCommandRegistry().registerCommand(new SetHomeCommand());
        this.getCommandRegistry().registerCommand(new DelHomeCommand());
        this.getCommandRegistry().registerCommand(new HomeCommand(this.teleportManager));
        this.getCommandRegistry().registerCommand(new HomesCommand());

        // TPA
        this.getCommandRegistry().registerCommand(new TpaCommand(this.tpaManager));
        this.getCommandRegistry().registerCommand(new TpacceptCommand(this.tpaManager, this.teleportManager));
        this.getCommandRegistry().registerCommand(new TpadenyCommand(this.tpaManager));
        this.getCommandRegistry().registerCommand(new TpaoffCommand());
        this.getCommandRegistry().registerCommand(new TpaonCommand());

        // Extra
        this.getCommandRegistry().registerCommand(new PluginReloadCommand(this));
        this.getCommandRegistry().registerCommand(new FreeCameraCommand());

        // Permissions
        this.getCommandRegistry().registerCommand(new PermissionsCommand());
        this.getCommandRegistry().registerCommand(new PermissionsManageCommand());
    }

    public void registerSystems() {
        this.getEntityStoreRegistry().registerSystem(new TeleportMovementCheckerSystem(this.teleportManager));
    }

    public void registerEvents() {
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEventHandler::onPlayerReady);

        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class,
                event -> PlayerEventHandler.onPlayerDisconnect(event, this.teleportManager)
        );

        this.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, _ -> this.pluginConfigManager.syncWorldSpawnProvider());
    }

    public void reloadConfig(PlayerRef requester) {
        this.pluginConfigManager.reload();
        this.warpManager.reload();

        Logger.info("Plugin configuration reloaded by " + requester.getUsername());
        requester.sendMessage(ColorUtil.colorize("&a[MultiCommands] Configurações recarregadas com sucesso!"));
    }
}
