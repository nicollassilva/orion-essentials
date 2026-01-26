package dev.thewarrior.MiniGames.Gaming.Game;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.World.WorldManager;

public class GameFactory {
    public static GameFactory instance;

    private WorldManager worldManager;

    public GameFactory(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public static GameFactory get() {
        return instance;
    }

    public Game create(GameType type) {
        return null;
    }
}
