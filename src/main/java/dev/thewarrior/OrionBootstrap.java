package dev.thewarrior;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Adapters.PermissionDataAdapter;
import dev.thewarrior.Essentials.Adapters.RegionAreaAdapter;
import dev.thewarrior.Essentials.Components.PlayerCommandComponent;
import dev.thewarrior.Essentials.EssentialsBootstrap;
import dev.thewarrior.Essentials.Events.RegionEntryProtectionSystem;
import dev.thewarrior.Essentials.Managers.Data.Permission.PermissionData;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionArea;
import dev.thewarrior.Essentials.Managers.*;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.SkyBlock.SkyBlockBootstrap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Random;

public class OrionBootstrap extends JavaPlugin {
    public static ComponentType<EntityStore, PlayerCommandComponent> PlayerDataComponent;
    public static Gson gson;
    public static Random random;

    private final EssentialsBootstrap essentialsBootstrap;
    //private final MiniGamesBootstrap miniGamesBootstrap;
    private final SkyBlockBootstrap skyBlockBootstrap;

    public OrionBootstrap(@NonNullDecl JavaPluginInit init) {
        super(init);

        this.essentialsBootstrap = new EssentialsBootstrap(this);
        //this.miniGamesBootstrap = new MiniGamesBootstrap(this);
        this.skyBlockBootstrap = new SkyBlockBootstrap(this);
    }

    @Override
    protected void setup() {
        super.setup();

        Logger.init(getLogger());
        Logger.info("~=~=~= Iniciando Orion Network ~=~=~=");

        this.essentialsBootstrap.setup();
        //this.miniGamesBootstrap.setup();
        this.skyBlockBootstrap.setup();
    }

    @Override
    public void start() {
        super.start();

        PlayerDataComponent = getEntityStoreRegistry().registerComponent(PlayerCommandComponent.class, "PlayerCommandData", PlayerCommandComponent.CODEC);

        this.essentialsBootstrap.start();
        //this.miniGamesBootstrap.start();
        this.skyBlockBootstrap.start();

        Logger.info("~=~=~= Orion Network iniciado com sucesso! ~=~=~=");
    }

    public PluginConfigManager getConfig() {
        return this.essentialsBootstrap.pluginConfigManager;
    }

    public PermissionManager getPermissionManager() {
        return this.essentialsBootstrap.permissionManager;
    }

    public RegionManager getRegionManager() {
        return this.essentialsBootstrap.regionManager;
    }

    public TeleportManager getTeleportManager() {
        return this.essentialsBootstrap.teleportManager;
    }

    public RegionEntryProtectionSystem getRegionEntryProtectionSystem() {
        return this.essentialsBootstrap.regionEntryProtectionSystem;
    }

    public PlayerHistoryManager getPlayerHistoryManager() {
        return this.essentialsBootstrap.playerHistoryManager;
    }

    public void reloadConfig(PlayerRef requester) {
        this.essentialsBootstrap.pluginConfigManager.reload();
        this.essentialsBootstrap.warpManager.reload();

        Logger.info("Reloaded by " + requester.getUsername());
        requester.sendMessage(ColorUtil.colorize("&a[OrionEssentials] Configurações recarregadas com sucesso!"));
    }

    static {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(PermissionData.class, new PermissionDataAdapter())
                .registerTypeAdapter(RegionArea.class, new RegionAreaAdapter())
                .create();

        random = new Random();
    }
}
