package dev.thewarrior.Essentials;

import com.hypixel.hytale.server.core.event.events.player.*;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import dev.thewarrior.BasePluginModule;
import dev.thewarrior.Essentials.Commands.Broadcast.BroadcastBaseCommand;
import dev.thewarrior.Essentials.Commands.Camera.FreeCameraCommand;
import dev.thewarrior.Essentials.Commands.Discord.DiscordCommand;
import dev.thewarrior.Essentials.Commands.Discord.SetDiscordCommand;
import dev.thewarrior.Essentials.Commands.Home.DelHomeCommand;
import dev.thewarrior.Essentials.Commands.Home.HomeCommand;
import dev.thewarrior.Essentials.Commands.Home.HomesCommand;
import dev.thewarrior.Essentials.Commands.Home.SetHomeCommand;
import dev.thewarrior.Essentials.Commands.Permissions.PermissionsCommand;
import dev.thewarrior.Essentials.Commands.Plugin.ReloadCommand;
import dev.thewarrior.Essentials.Commands.Region.RegionBaseCommand;
import dev.thewarrior.Essentials.Commands.Spawn.SetSpawnCommand;
import dev.thewarrior.Essentials.Commands.Spawn.SpawnCommand;
import dev.thewarrior.Essentials.Commands.Teleports.TpHereCommand;
import dev.thewarrior.Essentials.Commands.Tell.ReplyCommand;
import dev.thewarrior.Essentials.Commands.Tell.TellCommand;
import dev.thewarrior.Essentials.Commands.Tell.TellOffCommand;
import dev.thewarrior.Essentials.Commands.Tell.TellOnCommand;
import dev.thewarrior.Essentials.Commands.Tpa.*;
import dev.thewarrior.Essentials.Commands.Warp.BaseWarpCommand;
import dev.thewarrior.Essentials.Commands.Warp.DelWarpCommand;
import dev.thewarrior.Essentials.Commands.Warp.SetWarpCommand;
import dev.thewarrior.Essentials.Commands.Warp.WarpsCommand;
import dev.thewarrior.Essentials.Events.*;
import dev.thewarrior.Essentials.Handlers.PlayerChatEventHandler;
import dev.thewarrior.Essentials.Handlers.PlayerEventHandler;
import dev.thewarrior.Essentials.Managers.*;
import dev.thewarrior.OrionBootstrap;

public class EssentialsBootstrap extends BasePluginModule {
    public PluginConfigManager pluginConfigManager;
    public WarpManager warpManager;
    public TeleportManager teleportManager;
    public TpaManager tpaManager;
    public PermissionManager permissionManager;
    public RegionManager regionManager;
    public PlayerHistoryManager playerHistoryManager;

    public RegionEntryProtectionSystem regionEntryProtectionSystem;

    public EssentialsBootstrap(OrionBootstrap plugin) {
        super(plugin, "Essentials");
    }

    public void setup() {
        super.setup();

        this.pluginConfigManager = new PluginConfigManager(this.getDataDirectory());
        this.warpManager = new WarpManager(this.getDataDirectory());
        this.regionManager = new RegionManager(this.getDataDirectory());
        this.teleportManager = new TeleportManager(this.pluginConfigManager, this.regionManager);
        this.tpaManager = new TpaManager();
        this.permissionManager = new PermissionManager(this.getDataDirectory(), this.pluginConfigManager);
        this.playerHistoryManager = new PlayerHistoryManager(this.getDataDirectory());
    }

    public void start() {
        super.start();

        this.registerCommands();
        this.registerSystems();
        this.registerEvents();
    }

    public void registerCommands() {
        // Discord
        this.plugin.getCommandRegistry().registerCommand(new DiscordCommand(this.pluginConfigManager));
        this.plugin.getCommandRegistry().registerCommand(new SetDiscordCommand(this.pluginConfigManager));

        // Tell
        this.plugin.getCommandRegistry().registerCommand(new TellCommand());
        this.plugin.getCommandRegistry().registerCommand(new TellOnCommand());
        this.plugin.getCommandRegistry().registerCommand(new TellOffCommand());
        this.plugin.getCommandRegistry().registerCommand(new ReplyCommand());

        // Warps
        this.plugin.getCommandRegistry().registerCommand(new BaseWarpCommand(this.warpManager, this.teleportManager));
        this.plugin.getCommandRegistry().registerCommand(new WarpsCommand(this.warpManager));
        this.plugin.getCommandRegistry().registerCommand(new SetWarpCommand(this.warpManager));
        this.plugin.getCommandRegistry().registerCommand(new DelWarpCommand(this.warpManager));

        // Teleports
        this.plugin.getCommandRegistry().registerCommand(new TpHereCommand());

        // Spawn
        this.plugin.getCommandRegistry().registerCommand(new SpawnCommand(this.pluginConfigManager, this.teleportManager));
        this.plugin.getCommandRegistry().registerCommand(new SetSpawnCommand(this.pluginConfigManager, this.teleportManager));

        // Broadcast
        this.plugin.getCommandRegistry().registerCommand(new BroadcastBaseCommand(this.pluginConfigManager));

        // Home
        this.plugin.getCommandRegistry().registerCommand(new SetHomeCommand());
        this.plugin.getCommandRegistry().registerCommand(new DelHomeCommand());
        this.plugin.getCommandRegistry().registerCommand(new HomeCommand(this.teleportManager));
        this.plugin.getCommandRegistry().registerCommand(new HomesCommand());

        // TPA
        this.plugin.getCommandRegistry().registerCommand(new TpaCommand(this.tpaManager));
        this.plugin.getCommandRegistry().registerCommand(new TpacceptCommand(this.tpaManager, this.teleportManager));
        this.plugin.getCommandRegistry().registerCommand(new TpadenyCommand(this.tpaManager));
        this.plugin.getCommandRegistry().registerCommand(new TpaoffCommand());
        this.plugin.getCommandRegistry().registerCommand(new TpaonCommand());

        // Extra
        this.plugin.getCommandRegistry().registerCommand(new ReloadCommand(this.plugin));
        this.plugin.getCommandRegistry().registerCommand(new FreeCameraCommand());

        // Permissions
        this.plugin.getCommandRegistry().registerCommand(new PermissionsCommand(this.permissionManager));

        // Regions
        this.plugin.getCommandRegistry().registerCommand(new RegionBaseCommand(this.regionManager));
    }

    public void registerSystems() {
        this.plugin.getEntityStoreRegistry().registerSystem(new TeleportMovementCheckerSystem(this.teleportManager));

        // Regions Systems
        this.plugin.getEntityStoreRegistry().registerSystem(new ItemDropProtectionSystem(this.regionManager));
        this.plugin.getEntityStoreRegistry().registerSystem(new DamageProtectionSystem(this.regionManager));
        this.plugin.getEntityStoreRegistry().registerSystem(new BlockBreakProtectionSystem(this.regionManager));
        this.plugin.getEntityStoreRegistry().registerSystem(new BlockPlaceProtectionSystem(this.regionManager));
        this.plugin.getEntityStoreRegistry().registerSystem(new InteractionProtectionSystem(this.regionManager));
        this.plugin.getEntityStoreRegistry().registerSystem(new PickupItemProtectionSystem(this.regionManager));

        this.regionEntryProtectionSystem = new RegionEntryProtectionSystem(this.regionManager);

        this.plugin.getEntityStoreRegistry().registerSystem(this.regionEntryProtectionSystem);
    }

    public void registerEvents() {
        this.plugin.getEventRegistry().registerGlobal(PlayerConnectEvent.class, event -> PlayerEventHandler.onPlayerConnect(event, this.plugin));
        this.plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> PlayerEventHandler.onPlayerReady(event, this.plugin));
        this.plugin.getEventRegistry().registerGlobal(PlayerChatEvent.class, event -> PlayerChatEventHandler.onEvent(event, this.plugin));
        this.plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, event -> PlayerEventHandler.onPlayerDisconnect(event, this.plugin));

        this.plugin.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, event -> PlayerEventHandler.onPlayerAddedToWorld(event, this.plugin));
        this.plugin.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, _ -> this.pluginConfigManager.syncWorldSpawnProvider());
    }
}
