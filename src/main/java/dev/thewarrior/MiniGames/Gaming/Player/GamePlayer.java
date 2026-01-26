package dev.thewarrior.MiniGames.Gaming.Player;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class GamePlayer {
    private final UUID id;
    private final Map<GamePlayerStats, Integer> stats;
    private final AtomicReference<GamePlayerState> state;

    public GamePlayer(UUID id) {
        this.id = id;
        this.stats = new ConcurrentHashMap<>();
        this.state = new AtomicReference<>(GamePlayerState.WAITING);
    }

    public UUID getId() {
        return this.id;
    }

    public PlayerRef getPlayer() {
        return Universe.get().getPlayer(this.id);
    }

    public Map<GamePlayerStats, Integer> getStats() {
        return this.stats;
    }

    public GamePlayerState getState() {
        return this.state.get();
    }

    public void setState(final GamePlayerState expected, final GamePlayerState state) {
        this.state.compareAndSet(expected, state);
    }
}
