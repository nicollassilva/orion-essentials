package dev.thewarrior.MiniGames.Gaming;

import dev.thewarrior.MiniGames.Gaming.Arena.ArenaManager;
import dev.thewarrior.MiniGames.Gaming.Enums.GameState;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Enums.JoinResult;
import dev.thewarrior.MiniGames.Gaming.Handler.GameHandler;
import dev.thewarrior.MiniGames.Gaming.Handler.GameHandlerRegistry;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Gaming.Model.GameQueue;
import dev.thewarrior.MiniGames.Gaming.Model.QueuedPlayer;
import dev.thewarrior.MiniGames.Gaming.Player.PlayerSession;
import dev.thewarrior.MiniGames.Gaming.Player.PlayerSessionManager;
import dev.thewarrior.MiniGames.Gaming.Queue.QueueManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {
    private static final long TICK_RATE_MS = 50; // 20 TPS
    private static final long QUEUE_CHECK_MS = 1000;
    private static final int COUNTDOWN_SECONDS = 10;

    private final ArenaManager arenaManager;
    private final QueueManager queueManager;
    private final PlayerSessionManager sessionManager;
    private final GameHandlerRegistry handlerRegistry;

    private final ScheduledExecutorService scheduler;
    private final Map<UUID, ScheduledFuture<?>> countdownTasks;
    private volatile boolean running;

    public GameManager() {
        this.arenaManager = new ArenaManager();
        this.queueManager = new QueueManager();
        this.sessionManager = new PlayerSessionManager();
        this.handlerRegistry = new GameHandlerRegistry();
        this.countdownTasks = new ConcurrentHashMap<>();

        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        this.scheduler = Executors.newScheduledThreadPool(threads);
    }

    public void start() {
        if (running) return;
        running = true;

        scheduler.scheduleAtFixedRate(this::tickGames, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::processQueues, 0, QUEUE_CHECK_MS, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        running = false;
        countdownTasks.values().forEach(f -> f.cancel(false));
        countdownTasks.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // --- Player Queue Operations ---

    public JoinResult joinQueue(UUID playerId, GameType type) {
        if (sessionManager.isInGame(playerId)) {
            return JoinResult.ALREADY_IN_GAME;
        }
        if (queueManager.isInQueue(playerId)) {
            return JoinResult.ALREADY_IN_GAME;
        }

        // Tenta encontrar jogo em WAITING/STARTING que aceite jogadores
        Game available = arenaManager.findAvailableGame(type);
        if (available != null && tryJoinGame(playerId, available)) {
            return JoinResult.SUCCESS;
        }

        // Adiciona à fila
        if (queueManager.addToQueue(playerId, type)) {
            PlayerSession session = sessionManager.getOrCreate(playerId);
            session.setState(PlayerSession.PlayerState.IN_QUEUE);
            return JoinResult.SUCCESS;
        }

        return JoinResult.FAILED;
    }

    public void leaveQueue(UUID playerId) {
        queueManager.removeFromQueue(playerId);
        PlayerSession session = sessionManager.get(playerId);
        if (session != null && session.getState() == PlayerSession.PlayerState.IN_QUEUE) {
            session.setState(PlayerSession.PlayerState.LOBBY);
        }
    }

    // --- Game Join/Leave ---

    private boolean tryJoinGame(UUID playerId, Game game) {
        if (!game.addPlayer(playerId)) {
            return false;
        }

        PlayerSession session = sessionManager.getOrCreate(playerId);
        session.setCurrentGame(game.getId(), game.getType());
        session.setState(PlayerSession.PlayerState.IN_GAME);

        GameHandler handler = handlerRegistry.getHandler(game.getType());
        if (handler != null) {
            handler.onPlayerJoin(game, playerId);
        }

        checkGameStart(game);
        return true;
    }

    public void leaveGame(UUID playerId) {
        PlayerSession session = sessionManager.get(playerId);
        if (session == null || !session.isInGame()) return;

        Game game = arenaManager.findGame(session.getCurrentGameId());
        if (game == null) return;

        game.removePlayer(playerId);
        session.clearGame();
        session.setState(PlayerSession.PlayerState.LOBBY);

        GameHandler handler = handlerRegistry.getHandler(game.getType());
        if (handler != null) {
            handler.onPlayerLeave(game, playerId);
        }

        // Se estava em countdown e não tem mais mínimo, cancela
        if (game.getState() == GameState.STARTING && !game.hasMinimumPlayers()) {
            cancelCountdown(game);
            game.forceState(GameState.WAITING);
        }

        // Verifica se o jogo em andamento deve terminar
        if (game.getState() == GameState.RUNNING && game.getPlayerCount() < 2) {
            endGame(game);
        }
    }

    // --- Game Lifecycle ---

    private void checkGameStart(Game game) {
        if (game.getState() != GameState.WAITING) return;
        if (!game.hasMinimumPlayers()) return;

        if (game.tryTransitionTo(GameState.WAITING, GameState.STARTING)) {
            startCountdown(game);
        }
    }

    private void startCountdown(Game game) {
        AtomicInteger countdown = new AtomicInteger(COUNTDOWN_SECONDS);

        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            if (game.getState() != GameState.STARTING) {
                cancelCountdown(game);
                return;
            }

            int remaining = countdown.decrementAndGet();

            GameHandler handler = handlerRegistry.getHandler(game.getType());
            if (handler != null) {
                handler.onCountdownTick(game, remaining + 1);
            }

            if (remaining <= 0) {
                cancelCountdown(game);
                startGame(game);
            }
        }, 0, 1, TimeUnit.SECONDS);

        countdownTasks.put(game.getId(), task);
    }

    private void cancelCountdown(Game game) {
        ScheduledFuture<?> task = countdownTasks.remove(game.getId());
        if (task != null) {
            task.cancel(false);
        }
    }

    private void startGame(Game game) {
        if (!game.tryTransitionTo(GameState.STARTING, GameState.RUNNING)) {
            return;
        }

        game.setStartedAt(System.currentTimeMillis());

        GameHandler handler = handlerRegistry.getHandler(game.getType());
        if (handler != null) {
            handler.onGameStart(game);
        }
    }

    public void endGame(Game game) {
        GameState currentState = game.getState();
        if (currentState == GameState.ENDING) return;

        if (!game.tryTransitionTo(currentState, GameState.ENDING)) {
            return;
        }

        cancelCountdown(game);
        game.setEndedAt(System.currentTimeMillis());

        GameHandler handler = handlerRegistry.getHandler(game.getType());
        if (handler != null) {
            handler.onGameEnd(game);
        }

        for (UUID playerId : game.getPlayers()) {
            PlayerSession session = sessionManager.get(playerId);
            if (session != null) {
                session.clearGame();
                session.setState(PlayerSession.PlayerState.LOBBY);
            }
        }

        arenaManager.releaseArena(game);
    }

    // --- Tick System ---

    private void tickGames() {
        if (!running) return;

        for (GameType type : GameType.values()) {
            var pool = arenaManager.getPool(type);
            if (pool == null) continue;

            for (Game game : pool.getActiveArenas().values()) {
                if (game.getState() == GameState.RUNNING) {
                    tickGame(game);
                }
            }
        }
    }

    private void tickGame(Game game) {
        GameHandler handler = handlerRegistry.getHandler(game.getType());
        if (handler != null) {
            handler.tick(game);
        }
    }

    // --- Queue Processing ---

    private void processQueues() {
        if (!running) return;

        for (GameType type : GameType.values()) {
            processQueue(type);
        }
    }

    private void processQueue(GameType type) {
        GameQueue queue = queueManager.getQueue(type);
        if (queue == null || !queue.hasEnoughPlayers()) return;

        Game game = arenaManager.findAvailableGame(type);
        if (game == null) {
            game = arenaManager.acquireArena(type);
            if (game != null) {
                GameHandler handler = handlerRegistry.getHandler(type);
                if (handler != null) {
                    handler.onGameCreate(game);
                }
            }
        }
        if (game == null) return;

        while (!game.isFull() && queue.size() > 0) {
            QueuedPlayer queued = queue.poll();
            if (queued == null) break;

            queueManager.removeFromQueue(queued.playerId());
            tryJoinGame(queued.playerId(), game);
        }
    }

    // --- Player Disconnect ---

    public void onPlayerDisconnect(UUID playerId) {
        leaveQueue(playerId);
        leaveGame(playerId);
        sessionManager.remove(playerId);
    }

    // --- Getters ---

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public PlayerSessionManager getSessionManager() {
        return sessionManager;
    }

    public GameHandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }
}
