package dev.thewarrior.MiniGames.Gaming.Model;

import dev.thewarrior.MiniGames.Gaming.Enums.GameState;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Game {
    private final UUID id;
    private final GameType type;
    private final AtomicReference<GameState> state;
    private final Set<UUID> players;
    private final AtomicInteger playerCount;
    private final GameData data;
    private final long createdAt;

    private volatile long startedAt;
    private volatile long endedAt;

    public Game(GameType type) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.state = new AtomicReference<>(GameState.WAITING);
        this.players = ConcurrentHashMap.newKeySet();
        this.playerCount = new AtomicInteger(0);
        this.data = new GameData();
        this.createdAt = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public GameType getType() {
        return type;
    }

    public GameState getState() {
        return state.get();
    }

    // CAS para transição de estado segura
    public boolean tryTransitionTo(GameState expected, GameState newState) {
        return state.compareAndSet(expected, newState);
    }

    public void forceState(GameState newState) {
        state.set(newState);
    }

    public boolean addPlayer(UUID playerId) {
        if (!state.get().canJoin()) return false;
        if (playerCount.get() >= type.getMaxPlayers()) return false;

        if (players.add(playerId)) {
            playerCount.incrementAndGet();
            return true;
        }
        return false;
    }

    public boolean removePlayer(UUID playerId) {
        if (players.remove(playerId)) {
            playerCount.decrementAndGet();
            return true;
        }
        return false;
    }

    public boolean hasPlayer(UUID playerId) {
        return players.contains(playerId);
    }

    public int getPlayerCount() {
        return playerCount.get();
    }

    public boolean isFull() {
        return playerCount.get() >= type.getMaxPlayers();
    }

    public boolean hasMinimumPlayers() {
        return playerCount.get() >= type.getMinPlayers();
    }

    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(long endedAt) {
        this.endedAt = endedAt;
    }

    public GameData getData() {
        return data;
    }
}
