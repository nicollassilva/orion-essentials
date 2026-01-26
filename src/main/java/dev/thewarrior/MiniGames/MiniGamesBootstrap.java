package dev.thewarrior.MiniGames;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.BasePluginModule;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import dev.thewarrior.MiniGames.Gaming.Handler.Impl.BedWarsHandler;
import dev.thewarrior.MiniGames.Gaming.Handler.Impl.SkyWarsHandler;
import dev.thewarrior.MiniGames.Gaming.Handler.Impl.SpleefHandler;
import dev.thewarrior.MiniGames.Gaming.Handler.Impl.TntRunHandler;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.World.WorldManager;
import dev.thewarrior.OrionBootstrap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MiniGamesBootstrap extends BasePluginModule {
    private GamesSettingsStorage settingsStorage;
    private WorldManager worldManager;
    private GameManager gameManager;

    public MiniGamesBootstrap(OrionBootstrap plugin) {
        super(plugin, "MiniGames");
    }

    @Override
    public void setup() {
        super.setup();

        this.settingsStorage = new GamesSettingsStorage(this.getDataDirectory());
        this.worldManager = new WorldManager();
        this.gameManager = new GameManager();

        registerHandlers();
    }

    private void registerHandlers() {
        var registry = this.gameManager.getHandlerRegistry();
        registry.register(new TntRunHandler());
        registry.register(new BedWarsHandler());
        registry.register(new SkyWarsHandler());
        registry.register(new SpleefHandler());
    }

    @Override
    public void start() {
        super.start();

        this.worldManager.start();
        this.gameManager.start();

        this.plugin.getCommandRegistry().registerCommand(new JoinGameCommand(this.gameManager));
        this.plugin.getCommandRegistry().registerCommand(new LeaveGameCommand(this.gameManager));
    }

    public void stop() {
        if (this.gameManager != null) {
            this.gameManager.shutdown();
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public GamesSettingsStorage getSettingsStorage() {
        return settingsStorage;
    }

    public static class ReloadWorldCommand extends AbstractPlayerCommand {
        private final WorldManager worldManager;

        public ReloadWorldCommand(WorldManager worldManager) {
            super("rw", "rw");
            this.worldManager = worldManager;
        }

        @Override
        protected void execute(@NonNullDecl CommandContext ctx, @NonNullDecl Store<EntityStore> store,
                               @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef,
                               @NonNullDecl World world) {
            this.worldManager.start();
        }
    }
}
