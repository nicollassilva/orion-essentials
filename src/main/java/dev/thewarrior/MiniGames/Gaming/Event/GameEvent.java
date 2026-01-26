package dev.thewarrior.MiniGames.Gaming.Event;

import dev.thewarrior.MiniGames.Gaming.Model.Game;

import java.util.UUID;

public sealed interface GameEvent permits
        GameEvent.GameCreated,
        GameEvent.GameStarted,
        GameEvent.GameEnded,
        GameEvent.PlayerJoined,
        GameEvent.PlayerLeft {

    Game game();

    record GameCreated(Game game) implements GameEvent {}
    record GameStarted(Game game) implements GameEvent {}
    record GameEnded(Game game, UUID winnerId) implements GameEvent {}
    record PlayerJoined(Game game, UUID playerId) implements GameEvent {}
    record PlayerLeft(Game game, UUID playerId) implements GameEvent {}
}

