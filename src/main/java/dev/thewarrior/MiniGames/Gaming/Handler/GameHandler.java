package dev.thewarrior.MiniGames.Gaming.Handler;

import dev.thewarrior.MiniGames.Gaming.Enums.GameState;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;

import java.util.UUID;

public abstract class GameHandler {
    protected final GameType type;

    protected GameHandler(GameType type) {
        this.type = type;
    }

    public abstract void onGameCreate(Game game);
    public abstract void onCountdownTick(Game game, int secondsRemaining);
    public abstract void onGameStart(Game game);
    public abstract void onGameTick(Game game);
    public abstract void onGameEnd(Game game);
    public abstract void onPlayerJoin(Game game, UUID playerId);
    public abstract void onPlayerLeave(Game game, UUID playerId);

    public void tick(Game game) {
        if (game.getState() == GameState.RUNNING) {
            onGameTick(game);
        }
    }

    public GameType getType() {
        return type;
    }
}
