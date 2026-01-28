package dev.thewarrior.MiniGames;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.BasePluginModule;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.MiniGames.Gaming.Container.GameArenaFactory;
import dev.thewarrior.MiniGames.Gaming.Enums.GameJoinResult;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Enums.PlayerGameLeaveCause;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import dev.thewarrior.MiniGames.Gaming.Systems.PlayerDeathSystem;
import dev.thewarrior.MiniGames.Storage.GamesPrefabsStorage;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.World.WorldManager;
import dev.thewarrior.OrionBootstrap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.atomic.AtomicInteger;

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

        this.worldManager = new WorldManager(this.settingsStorage);
        this.gameManager = new GameManager(this.settingsStorage, this.worldManager);
    }

    public void start() {
        super.start();

        GameArenaFactory.init(this.settingsStorage, this.prefabsStorage, this.worldManager);

        this.worldManager.start();
        this.gameManager.start();

        this.plugin.getEntityStoreRegistry().registerSystem(new PlayerDeathSystem(this.gameManager));

        this.plugin.getCommandRegistry().registerCommand(new ReloadWorldCommand(this.worldManager));
        this.plugin.getCommandRegistry().registerCommand(new GenerateTerrainCommand(this.worldManager, this.settingsStorage));
        this.plugin.getCommandRegistry().registerCommand(new JoinGameCommand(this.gameManager));

        this.plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, event -> {
            this.gameManager.removePlayerData(event.getPlayerRef().getUuid(), PlayerGameLeaveCause.DISCONNECTED);
        });
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

    public static class JoinGameCommand extends AbstractPlayerCommand {
        private RequiredArg<String> game;
        private GameManager gameManager;
        public JoinGameCommand(GameManager gameManager) {
            super("join", "rw");
            this.gameManager = gameManager;
            this.game = this.withRequiredArg("game", "The game to join", ArgTypes.STRING);
        }

        @Override
        protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
            GameJoinResult result = this.gameManager.joinGameQueue(playerRef, GameType.valueOf(this.game.get(commandContext).toUpperCase()));

            switch (result) {
                case SUCCESS ->
                        playerRef.sendMessage(ColorUtil.colorize("&aVocê entrou na fila do jogo!"));
                case ALREADY_IN_QUEUE ->
                        playerRef.sendMessage(ColorUtil.colorize("&eVocê já está na fila de um jogo!"));
                case ALREADY_IN_GAME ->
                        playerRef.sendMessage(ColorUtil.colorize("&cVocê já está em um jogo!"));
                case GAME_NOT_AVAILABLE ->
                        playerRef.sendMessage(ColorUtil.colorize("&cO jogo selecionado não está disponível!"));
                case GAME_FULL ->
                        playerRef.sendMessage(ColorUtil.colorize("&cO jogo está cheio no momento!"));
                case FAILED ->
                        playerRef.sendMessage(ColorUtil.colorize("&cFalha ao entrar na fila do jogo! Tente novamente mais tarde."));
            }
        }
    }

    public static class GenerateTerrainCommand extends AbstractPlayerCommand {
        private WorldManager worldManager;
        private GamesSettingsStorage settingsStorage;
        public GenerateTerrainCommand(WorldManager worldManager, GamesSettingsStorage settingsStorage) {
            super("gt", "gt");
            this.worldManager = worldManager;
            this.settingsStorage = settingsStorage;
        }

        @Override
        protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
            commandContext.sendMessage(ColorUtil.colorize("&a Starting to generate TNT Run arenas..."));

            final AtomicInteger counter = new AtomicInteger(0);

            HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
                world.execute(() -> {
                    final Vector3i generatedPosition = this.worldManager.getWorldTerrainManager().generateCenterPosition(world, this.settingsStorage.getByType(GameType.TNT_RUN));

                    try {
                        final BlockSelection prefab = PrefabStore.get().getServerPrefab("tntrun_arena1.prefab.json");

                        prefab.place(null, world, generatedPosition, null);

                        counter.incrementAndGet();
                        commandContext.sendMessage(ColorUtil.colorize(counter.get() + "]: &a Placing minigame map at " + generatedPosition.toString()));
                    } catch (Exception e) {
                        Logger.error("Failed to place prefab: " + e.getMessage());
                    }
                });
            }, 2000, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
}
