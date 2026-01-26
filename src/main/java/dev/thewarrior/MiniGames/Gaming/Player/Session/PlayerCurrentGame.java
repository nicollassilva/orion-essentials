package dev.thewarrior.MiniGames.Gaming.Player.Session;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.UUID;

public record PlayerCurrentGame(UUID gameId, GameType type, long queuedAt) {
    public PlayerCurrentGame(final UUID gameId, final GameType type) {
        this(gameId, type, System.currentTimeMillis());
    }
}
