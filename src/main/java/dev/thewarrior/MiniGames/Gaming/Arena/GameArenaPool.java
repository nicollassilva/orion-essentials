package dev.thewarrior.MiniGames.Gaming.Arena;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameArenaPool {
    private final GameType type;
    private final int maxInstances;

    private final Queue<Game> availableArenas;
    private final Map<UUID, Game> activeArenas;

    public GameArenaPool(GameType type, GameSettings settings) {
        this.type = type;
        this.availableArenas = new ConcurrentLinkedQueue<>();
        this.activeArenas = new ConcurrentHashMap<>();
        this.maxInstances = settings.getMaxInstances();
    }

    public Game acquire() {
        Game arena = this.availableArenas.poll();

        if (arena == null && this.canBeCreated()) {
            //arena = createArena();
        }

        if (arena == null) {
            return null;
        }

        this.activeArenas.put(arena.getId(), arena);
        return arena;
    }

    protected boolean canBeCreated() {
        return (this.availableArenas.size() + this.activeArenas.size()) < this.maxInstances;
    }
}
