package dev.thewarrior.MiniGames.Gaming.Handler.Impl;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Handler.GameHandler;
import dev.thewarrior.MiniGames.Gaming.Model.Game;

import java.util.UUID;

public class SpleefHandler extends GameHandler {

    public SpleefHandler() {
        super(GameType.SPLEEF);
    }

    @Override
    public void onGameCreate(Game game) {
    }

    @Override
    public void onCountdownTick(Game game, int secondsRemaining) {
    }

    @Override
    public void onGameStart(Game game) {
    }

    @Override
    public void onGameTick(Game game) {
    }

    @Override
    public void onGameEnd(Game game) {
    }

    @Override
    public void onPlayerJoin(Game game, UUID playerId) {
    }

    @Override
    public void onPlayerLeave(Game game, UUID playerId) {
    }
}

