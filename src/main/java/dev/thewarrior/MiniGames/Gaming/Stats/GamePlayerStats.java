package dev.thewarrior.MiniGames.Gaming.Stats;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GamePlayerStats {
    private final UUID playerId;
    private final UUID gameId;
    private final Map<String, AtomicInteger> intStats;
    private final Map<String, AtomicLong> longStats;
    private final long joinedAt;

    private volatile boolean alive;
    private volatile int finalPlacement;

    public GamePlayerStats(UUID playerId, UUID gameId) {
        this.playerId = playerId;
        this.gameId = gameId;
        this.intStats = new ConcurrentHashMap<>();
        this.longStats = new ConcurrentHashMap<>();
        this.joinedAt = System.currentTimeMillis();
        this.alive = true;
    }

    public void incrementInt(String key) {
        intStats.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void addInt(String key, int value) {
        intStats.computeIfAbsent(key, k -> new AtomicInteger(0)).addAndGet(value);
    }

    public int getInt(String key) {
        AtomicInteger value = intStats.get(key);
        return value != null ? value.get() : 0;
    }

    public void incrementLong(String key) {
        longStats.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void addLong(String key, long value) {
        longStats.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(value);
    }

    public long getLong(String key) {
        AtomicLong value = longStats.get(key);
        return value != null ? value.get() : 0L;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getGameId() {
        return gameId;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getFinalPlacement() {
        return finalPlacement;
    }

    public void setFinalPlacement(int placement) {
        this.finalPlacement = placement;
    }
}

