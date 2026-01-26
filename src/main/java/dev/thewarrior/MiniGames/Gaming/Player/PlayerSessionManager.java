package dev.thewarrior.MiniGames.Gaming.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSessionManager {
    private final Map<UUID, PlayerSession> sessions;

    public PlayerSessionManager() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public PlayerSession getOrCreate(UUID playerId) {
        return sessions.computeIfAbsent(playerId, PlayerSession::new);
    }

    public PlayerSession get(UUID playerId) {
        return sessions.get(playerId);
    }

    public void remove(UUID playerId) {
        sessions.remove(playerId);
    }

    public boolean isInGame(UUID playerId) {
        PlayerSession session = sessions.get(playerId);
        return session != null && session.isInGame();
    }

    public UUID getCurrentGame(UUID playerId) {
        PlayerSession session = sessions.get(playerId);
        return session != null ? session.getCurrentGameId() : null;
    }

    public int getOnlineCount() {
        return sessions.size();
    }
}

