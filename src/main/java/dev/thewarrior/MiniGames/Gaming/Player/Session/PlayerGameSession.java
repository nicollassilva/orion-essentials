package dev.thewarrior.MiniGames.Gaming.Player.Session;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerGameSession {
    private final UUID playerId;
    private final AtomicReference<PlayerCurrentGame> currentGame;
    private final AtomicReference<PlayerGameSessionState> currentState;
    private final long joinedAt;

    public PlayerGameSession(final UUID playerId) {
        this.playerId = playerId;
        this.currentState = new AtomicReference<>(PlayerGameSessionState.LOBBY);
        this.currentGame = new AtomicReference<>(null);
        this.joinedAt = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public PlayerCurrentGame getCurrentGame() {
        return currentGame.get();
    }

    public boolean trySetCurrentGame(final PlayerCurrentGame game) {
        if (game == null) {
            return false;
        }

        return this.currentGame.compareAndSet(null, game);
    }

    public PlayerGameSessionState getCurrentState() {
        return currentState.get();
    }

    public void setCurrentState(final PlayerGameSessionState expected, final PlayerGameSessionState state) {
        this.currentState.compareAndSet(expected, state);
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public PlayerRef getPlayer() {
        return Universe.get().getPlayer(this.playerId);
    }
}
