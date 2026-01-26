package dev.thewarrior.MiniGames.Gaming.Container;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

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

    public GameContainer(GameType type, GameSettings settings) {
        this.type = type;
        this.availableGames = new ConcurrentLinkedQueue<>();
        this.activeGames = new ConcurrentHashMap<>();
        this.maxInstances = settings.getMaxInstances();
    }

    public Game acquire() {
        Game arena = this.availableGames.poll();

        if (arena == null && this.canBeCreated()) {
            arena = this.createGame();
        }

        if (arena != null) {
            this.activeGames.put(arena.getId(), arena);
        }

        return arena;
    }

    private Game createGame() {
        return null;
    }

    protected boolean canBeCreated() {
        return (this.availableGames.size() + this.activeGames.size()) < this.maxInstances;
    }
}
