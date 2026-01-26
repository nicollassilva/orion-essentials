package dev.thewarrior.MiniGames.Gaming.Player;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.UUID;

public class PlayerSession {
    private final UUID playerId;
    private volatile UUID currentGameId;
    private volatile GameType currentGameType;
    private volatile PlayerState state;
    private final long joinedAt;

    public PlayerSession(UUID playerId) {
        this.playerId = playerId;
        this.state = PlayerState.LOBBY;
        this.joinedAt = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getCurrentGameId() {
        return currentGameId;
    }

    public void setCurrentGame(UUID gameId, GameType type) {
        this.currentGameId = gameId;
        this.currentGameType = type;
    }

    public void clearGame() {
        this.currentGameId = null;
        this.currentGameType = null;
    }

    public GameType getCurrentGameType() {
        return currentGameType;
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public boolean isInGame() {
        return currentGameId != null;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public enum PlayerState {
        LOBBY,
        IN_QUEUE,
        IN_GAME,
        SPECTATING
    }
}

