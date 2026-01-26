package dev.thewarrior.MiniGames.Gaming.Arena;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ArenaPool {
    private final GameType type;
    private final Queue<Game> availableArenas;
    private final Map<UUID, Game> activeArenas;
    private final AtomicInteger totalCreated;
    private final int maxPoolSize;

    public ArenaPool(GameType type) {
        this.type = type;
        this.availableArenas = new ConcurrentLinkedQueue<>();
        this.activeArenas = new ConcurrentHashMap<>();
        this.totalCreated = new AtomicInteger(0);
        this.maxPoolSize = type.getArenaPoolSize();

        warmUp();
    }

    private void warmUp() {
        int initialSize = Math.min(2, maxPoolSize);
        for (int i = 0; i < initialSize; i++) {
            availableArenas.offer(createArena());
        }
    }

    private Game createArena() {
        totalCreated.incrementAndGet();
        return new Game(type);
    }

    // Obtém uma arena disponível ou cria uma nova se necessário
    public Game acquire() {
        Game arena = availableArenas.poll();
        if (arena == null && totalCreated.get() < maxPoolSize) {
            arena = createArena();
        }
        if (arena == null) {
            return null;
        }
        activeArenas.put(arena.getId(), arena);
        return arena;
    }

    // Devolve a arena ao pool para reutilização
    public void release(Game arena) {
        activeArenas.remove(arena.getId());

        // Recria a arena para estado limpo
        Game fresh = createArena();
        availableArenas.offer(fresh);
    }

    public Game getActive(UUID arenaId) {
        return activeArenas.get(arenaId);
    }

    public int getAvailableCount() {
        return availableArenas.size();
    }

    public int getActiveCount() {
        return activeArenas.size();
    }

    public GameType getType() {
        return type;
    }

    public Map<UUID, Game> getActiveArenas() {
        return activeArenas;
    }
}

