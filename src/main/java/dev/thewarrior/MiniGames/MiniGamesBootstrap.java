package dev.thewarrior.MiniGames;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.BasePluginModule;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.World.WorldManager;
import dev.thewarrior.OrionBootstrap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MiniGamesBootstrap extends BasePluginModule {
    public GamesSettingsStorage settingsStorage;

    public WorldManager worldManager;

    public MiniGamesBootstrap(OrionBootstrap plugin) {
        super(plugin, "MiniGames");
    }

    public void setup() {
        super.setup();

        this.settingsStorage = new GamesSettingsStorage(this.getDataDirectory());

        this.worldManager = new WorldManager();
    }

    public void start() {
        super.start();

        this.worldManager.start();

        this.plugin.getCommandRegistry().registerCommand(new ReloadWorldCommand(this.worldManager));
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
