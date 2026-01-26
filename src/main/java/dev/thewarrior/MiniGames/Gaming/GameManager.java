package dev.thewarrior.MiniGames.Gaming;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.MiniGames.Gaming.Arena.GameArenaManager;
import dev.thewarrior.MiniGames.Gaming.Enums.GameJoinResult;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Player.Session.PlayerGameSessionManager;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;
import dev.thewarrior.MiniGames.World.WorldManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameManager {
    private static final long TICK_RATE_MS = 50; // 20 TPS
    private static final long QUEUE_CHECK_MS = 1000;
    private static final int COUNTDOWN_SECONDS = 10;

    private final GamesSettingsStorage settingsStorage;

    private final GameArenaManager arenaManager;
    private final WorldManager worldManager;

    private final PlayerGameSessionManager playerSessionManager;

    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public GameManager(final GamesSettingsStorage settingsStorage, final WorldManager worldManager) {
        this.settingsStorage = settingsStorage;

        this.worldManager = worldManager;
        this.arenaManager = new GameArenaManager(settingsStorage);

        this.playerSessionManager = new PlayerGameSessionManager();

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

        this.scheduler.shutdown();

        try {
            if(!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
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

        // TODO: Add player to the game queue

        return GameJoinResult.SUCCESS;
    }

    private void onGamesTick() {
        // Logic to update active games
    }

    private void onQueuesTick() {
        // Logic to check player queues and start countdowns
    }
}
