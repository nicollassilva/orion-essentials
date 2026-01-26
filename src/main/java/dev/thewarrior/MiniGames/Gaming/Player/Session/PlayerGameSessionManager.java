package dev.thewarrior.MiniGames.Gaming.Player.Session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerGameSessionManager {
    private final Map<UUID, PlayerGameSession> sessions;

    public PlayerGameSessionManager() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public PlayerGameSession getOrCreateSession(final UUID playerId) {
        return this.sessions.computeIfAbsent(playerId, PlayerGameSession::new);
    }

    public PlayerGameSession getSession(final UUID playerId) {
        return this.sessions.getOrDefault(playerId, null);
    }

    public void removeSession(final UUID playerId) {
        this.sessions.remove(playerId);
    }

    public boolean isInGame(final UUID playerId) {
        final PlayerGameSession session = this.sessions.getOrDefault(playerId, null);

        return session != null && session.getCurrentGame() != null;
    }
}
