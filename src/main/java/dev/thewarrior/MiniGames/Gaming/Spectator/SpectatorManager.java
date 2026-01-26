package dev.thewarrior.MiniGames.Gaming.Spectator;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpectatorManager {
    private final Set<UUID> spectators;

    public SpectatorManager() {
        this.spectators = ConcurrentHashMap.newKeySet();
    }

    public boolean addSpectator(UUID playerId) {
        return spectators.add(playerId);
    }

    public boolean removeSpectator(UUID playerId) {
        return spectators.remove(playerId);
    }

    public boolean isSpectator(UUID playerId) {
        return spectators.contains(playerId);
    }

    public Set<UUID> getSpectators() {
        return spectators;
    }

    public int getSpectatorCount() {
        return spectators.size();
    }

    public void clear() {
        spectators.clear();
    }
}

