package dev.thewarrior.MiniGames.Gaming.Model;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.UUID;

public record QueuedPlayer(UUID playerId, GameType gameType, long queuedAt) {
    public QueuedPlayer(UUID playerId, GameType gameType) {
        this(playerId, gameType, System.currentTimeMillis());
    }
}

