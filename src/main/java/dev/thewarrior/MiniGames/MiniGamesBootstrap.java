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
import dev.thewarrior.MiniGames.Storage.GamesPrefabsStorage;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.World.WorldManager;
import dev.thewarrior.OrionBootstrap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MiniGamesBootstrap extends BasePluginModule {
    private GamesSettingsStorage settingsStorage;
    private GamesPrefabsStorage prefabsStorage;

    private WorldManager worldManager;
    private GameManager gameManager;

    public MiniGamesBootstrap(OrionBootstrap plugin) {
        super(plugin, "MiniGames");
    }

    public void setup() {
        super.setup();

        this.prefabsStorage = new GamesPrefabsStorage(this.getDataDirectory());
        this.settingsStorage = new GamesSettingsStorage(this.getDataDirectory());

        this.worldManager = new WorldManager();
        this.gameManager = new GameManager(this.settingsStorage, this.worldManager);
    }

    public void start() {
        super.start();

        this.worldManager.start();
        this.gameManager.start();

        this.plugin.getCommandRegistry().registerCommand(new ReloadWorldCommand(this.worldManager));
    }

    public void stop() {
        super.stop();

        this.gameManager.shutdown();
    }

    public GamesSettingsStorage getSettingsStorage() {
        return this.settingsStorage;
    }

    public WorldManager getWorldManager() {
        return this.worldManager;
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public GamesPrefabsStorage getPrefabsStorage() {
        return this.prefabsStorage;
    }

    public static class ReloadWorldCommand extends AbstractPlayerCommand {
        private WorldManager worldManager;
        public ReloadWorldCommand(WorldManager worldManager) {
            super("rw", "rw");
            this.worldManager = worldManager;
        }

        @Override
        protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
            this.worldManager.start();
        }
    }
}
