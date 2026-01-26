package dev.thewarrior.MiniGames.Gaming.Container;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

public class GameArenaFactory {
    public static GameArenaFactory instance;

    public static GameArenaFactory get() {
        if (instance == null) {
            instance = new GameArenaFactory();
        }

        return instance;
    }

    public GameArena createArena(GameType type) {
        return null;
    }
}
