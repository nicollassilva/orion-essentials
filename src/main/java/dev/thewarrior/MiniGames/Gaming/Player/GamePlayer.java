package dev.thewarrior.MiniGames.Gaming.Player;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nullable;
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

    @Nullable
    public PlayerRef getPlayer() {
        return Universe.get().getPlayer(this.id);
    }

    public Map<GamePlayerStats, Integer> getStats() {
        return this.stats;
    }

    public GamePlayerState getState() {
        return this.state.get();
    }

    public boolean setState(final GamePlayerState expected, final GamePlayerState state) {
        return this.state.compareAndSet(expected, state);
    }

    public void addOrUpdateStat(final GamePlayerStats stat, final int value) {
        this.stats.merge(stat, value, Integer::sum);
    }
}
