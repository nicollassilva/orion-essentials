package dev.thewarrior.MiniGames.Gaming;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.MiniGames.Gaming.Container.GameContainerManager;
import dev.thewarrior.MiniGames.Gaming.Enums.GameJoinResult;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Enums.PlayerGameLeaveCause;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Gaming.Player.Session.PlayerCurrentGame;
import dev.thewarrior.MiniGames.Gaming.Player.Session.PlayerGameSession;
import dev.thewarrior.MiniGames.Gaming.Player.Session.PlayerGameSessionManager;
import dev.thewarrior.MiniGames.Gaming.Player.Session.PlayerGameSessionState;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;
import dev.thewarrior.MiniGames.World.WorldManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameManager {
    public static final long TICK_RATE_MS = 50; // 20 TPS
    public static final long QUEUE_CHECK_MS = 1000;
    public static final int PLAYER_QUEUES_TRIES = 3;
    public static final int COUNTDOWN_SECONDS = 2;

    private final GamesSettingsStorage settingsStorage;

    private final GameContainerManager containerManager;
    private final WorldManager worldManager;

    private final PlayerGameSessionManager playerSessionManager;

    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final Map<UUID, GameType> playerQueues;
    private final Map<UUID, Integer> playerQueueTries;

    private final AtomicBoolean isAlive = new AtomicBoolean(true);

    public GameManager(final GamesSettingsStorage settingsStorage, final WorldManager worldManager) {
        this.settingsStorage = settingsStorage;

        this.worldManager = worldManager;
        this.containerManager = new GameContainerManager(settingsStorage);

        this.playerSessionManager = new PlayerGameSessionManager();

        this.playerQueues = new ConcurrentHashMap<>();
        this.playerQueueTries = new ConcurrentHashMap<>();

        this.scheduler = Executors.newScheduledThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
        );
    }

    public void start() {
        if(!this.running.compareAndSet(false, true)) return;

        this.scheduler.scheduleWithFixedDelay(this::onGamesTick, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
        this.scheduler.scheduleWithFixedDelay(this::onQueuesTick, 0, QUEUE_CHECK_MS, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if(!this.running.compareAndSet(true, false)) return;

        this.isAlive.set(false);
        this.scheduler.shutdownNow();
    }

    public GameJoinResult joinGameQueue(final PlayerRef playerRef, final GameType type) {
        if(this.playerSessionManager.isInGame(playerRef.getUuid())) {
            return GameJoinResult.ALREADY_IN_GAME;
        }

        final GameSettings settings = this.settingsStorage.getByType(type);

        if(settings == null || settings.isDisabled()) {
            Logger.warning(playerRef.getUsername() + " tried to join unavailable game type: " + type);
            return GameJoinResult.GAME_NOT_AVAILABLE;
        }

        if(settings.getPermissionsRequired() != null && !settings.getPermissionsRequired().isEmpty()) {
            final Ref<EntityStore> ref = playerRef.getReference();

            if(ref == null || !ref.isValid()) {
                Logger.warning("Failed to get player store for permission check.");

                return GameJoinResult.FAILED;
            }

            final Store<EntityStore> store = ref.getStore();
            final Player player = store.getComponent(ref, Player.getComponentType());

            if(player == null || player.wasRemoved()) {
                Logger.warning("Failed to get player component for permission check.");

                return GameJoinResult.FAILED;
            }

            for (final String permission : settings.getPermissionsRequired()) {
                if(player.hasPermission(permission)) continue;

                return GameJoinResult.GAME_NOT_AVAILABLE;
            }
        }

        this.playerQueues.put(playerRef.getUuid(), type);

        return GameJoinResult.SUCCESS;
    }

    private void onGamesTick() {
        if(!this.isAlive.get()) return;

        this.containerManager.tickActiveGames();
    }

    private void onQueuesTick() {
        System.out.println("Checking player queues...");

        if(!this.playerQueues.isEmpty()) {
            for (Map.Entry<UUID, GameType> entry : this.playerQueues.entrySet()) {
                if(!this.isAlive.get()) return;

                final UUID playerId = entry.getKey();
                final GameType gameType = entry.getValue();

                final Game gameInstance = this.attemptToStartGameForPlayer(playerId, gameType);

                if(gameInstance != null) continue;

                if (!this.playerQueueTries.containsKey(playerId)) {
                    this.playerQueueTries.put(playerId, 1);
                    continue;
                }

                final int currentTries = this.playerQueueTries.get(playerId);

                if (currentTries >= PLAYER_QUEUES_TRIES) {
                    this.removePlayerData(playerId, PlayerGameLeaveCause.KICKED_BY_SYSTEM);

                    Logger.info("Removed player " + playerId + " from queue for game type " + gameType + " due to timeout.");
                } else {
                    this.playerQueueTries.put(playerId, currentTries + 1);
                }

                Logger.info("Failed to start game for player " + playerId + " of type " + gameType);
            }
        }

        if(!this.isAlive.get()) return;

        this.containerManager.tickQueuedGames();
    }

    public void removePlayerData(final UUID playerId, final PlayerGameLeaveCause leaveCause) {
        this.playerQueues.remove(playerId);
        this.playerQueueTries.remove(playerId);

        final PlayerGameSession session = this.playerSessionManager.getSession(playerId);

        if(session == null) {
            Logger.warning("Tried to remove player " + playerId + " but session not found");
            return;
        }

        this.playerSessionManager.removeSession(playerId);

        final PlayerCurrentGame currentGame = session.getCurrentGame();

        if(currentGame != null) {
            final Game playerGame = this.containerManager.getGame(currentGame);

            if(playerGame != null) {
                playerGame.onPlayerLeave(playerId, leaveCause);
            } else {
                Logger.warning("Player " + playerId + " was in game " + currentGame.gameId() + " but game not found in container");
            }
        } else {
            Logger.warning("Player " + playerId + " session had no current game set");
        }
    }

    public boolean handlePlayerDeath(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl DeathComponent deathComponent,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer
    ) {
        final UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

        if(uuidComponent == null) return false;

        final UUID playerId = uuidComponent.getUuid();
        final PlayerGameSession session = this.playerSessionManager.getSession(playerId);

        if(session == null) return false;

        final PlayerCurrentGame currentGame = session.getCurrentGame();

        if(currentGame == null) return false;

        final Game game = this.containerManager.getGame(currentGame);

        if(game == null) return false;

        // Ask the game if the player should be eliminated
        final boolean shouldEliminate = game.onPlayerDeath(playerId);

        // If game says to eliminate, remove player data
        if(shouldEliminate) {
            this.removePlayerData(playerId, PlayerGameLeaveCause.LOST_GAME);
        }

        return true;
    }


    private Game attemptToStartGameForPlayer(final UUID playerId, final GameType gameType) {
        final PlayerGameSession sessionStarted = this.playerSessionManager.getOrCreateSession(playerId);

        if(sessionStarted == null || sessionStarted.getCurrentState() != PlayerGameSessionState.LOBBY) return null;

        final Game game = this.containerManager.acquireGame(gameType);

        if(game == null) return null;

        // Try to set current game - if fails, player is already in a game
        final boolean gameSet = sessionStarted.trySetCurrentGame(new PlayerCurrentGame(game.getId(), gameType));

        if (!gameSet) {
            // Player is already in another game, return game to pool
            Logger.warning("Failed to set game for player " + playerId + ": already in game");
            return null;
        }

        game.onPlayerJoin(sessionStarted);

        this.playerQueues.remove(playerId);
        this.playerQueueTries.remove(playerId);

        return game;
    }
}
