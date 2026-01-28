package dev.thewarrior.MiniGames.Gaming.Container;

import dev.thewarrior.MiniGames.Gaming.Enums.GameState;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Games.TNTRunGame;
import dev.thewarrior.MiniGames.Gaming.Games.TNTTagGame;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameContainer {
    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private final GameType type;
    private final int maxInstances;

    private final Queue<Game> availableGames;
    private final Map<UUID, Game> activeGames;

    private final GameSettings settings;

    public GameContainer(GameType type, GameSettings settings) {
        this.type = type;
        this.settings = settings;
        this.availableGames = new ConcurrentLinkedQueue<>();
        this.activeGames = new ConcurrentHashMap<>();
        this.maxInstances = settings.getMaxInstances();
    }

    public void tickActiveGames() {
        for (Game game : this.activeGames.values()) {
            if(game.getState().canJoin() || game.getState() == GameState.COUNTDOWN) continue;

            try {
                game.onGameTick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Limpar games terminados
        this.cleanupFinishedGames();
    }

    public void tickQueuedGames() {
        for (Game game : this.activeGames.values()) {
            if(!game.getState().isQueuedTick()) continue;

            game.onCountdownTick();
        }
    }

    /**
     * Remove games que já terminaram (estado ENDING) e retorna para o pool
     */
    private void cleanupFinishedGames() {
        final List<UUID> finishedGameIds = new ArrayList<>();

        for (final Game game : this.activeGames.values()) {
            if (game.getState() == GameState.ENDED) {
                finishedGameIds.add(game.getId());
            }
        }

        for (final UUID gameId : finishedGameIds) {
            final Game game = this.activeGames.remove(gameId);

            if (game != null) {
                game.reset();

                this.availableGames.offer(game);
            }
        }
    }

    public Game acquire(boolean isRetry) {
        Game game = this.availableGames.poll();

        if (game == null && this.canBeCreated()) {
            game = this.createGame();
        }

        if(game != null) {
            if((!game.getState().canJoin() || game.getPlayers().size() >= this.settings.getMaxPlayersPerGame())) {
                if(isRetry) {
                    game.reset();
                    this.availableGames.offer(game);
                    return null;
                } else {
                    return this.acquire(true);
                }
            }

            this.activeGames.put(game.getId(), game);
        }

        return game;
    }

    private Game createGame() {
        final GameArena arena = GameArenaFactory.get().createArena(this.type);

        if (arena == null) return null;

        final Game game = switch (this.type) {
            case TNT_RUN -> new TNTRunGame(this.type, arena, this.settings);
            case TNT_TAG -> new TNTTagGame(this.type, arena, this.settings);
            default -> null;
        };

        if(game == null) return null;

        game.onGameCreated();

        this.activeGames.put(game.getId(), game);

        return game;
    }

    public Game getGame(final UUID gameId) {
        return this.activeGames.get(gameId);
    }

    protected boolean canBeCreated() {
        return (this.availableGames.size() + this.activeGames.size()) < this.maxInstances;
    }
}
