package dev.thewarrior.MiniGames.Gaming.Stats;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameStatsManager {
    private final Map<UUID, Map<UUID, GamePlayerStats>> gameStats; // gameId -> playerId -> stats

    public GameStatsManager() {
        this.gameStats = new ConcurrentHashMap<>();
    }

    public GamePlayerStats getOrCreate(UUID gameId, UUID playerId) {
        return gameStats
                .computeIfAbsent(gameId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(playerId, k -> new GamePlayerStats(playerId, gameId));
    }

    public GamePlayerStats get(UUID gameId, UUID playerId) {
        Map<UUID, GamePlayerStats> players = gameStats.get(gameId);
        return players != null ? players.get(playerId) : null;
    }

    public Map<UUID, GamePlayerStats> getGameStats(UUID gameId) {
        return gameStats.get(gameId);
    }

    public void clearGame(UUID gameId) {
        gameStats.remove(gameId);
    }
}

