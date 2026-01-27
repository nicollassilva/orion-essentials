package dev.thewarrior.MiniGames.Gaming.Container;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Gaming.Player.Session.PlayerCurrentGame;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

import java.util.EnumMap;
import java.util.Map;

public class GameContainerManager {
    private final Map<GameType, GameContainer> games;

    public GameContainerManager(final GamesSettingsStorage settingsStorage) {
        this.games = new EnumMap<>(GameType.class);

        for (GameType type : GameType.values()) {
            final GameSettings settings = settingsStorage.getByType(type);

            if(settings == null || settings.isDisabled()) continue;

            this.games.put(type, new GameContainer(type, settings));
        }
    }

    // Tick games that are currently active/in-progress
    public void tickActiveGames() {
        for (GameContainer pool : this.games.values()) {
            pool.tickActiveGames();
        }
    }

    // Tick games that are in the queue/waiting state
    public void tickQueuedGames() {
        for (GameContainer pool : this.games.values()) {
            pool.tickQueuedGames();
        }
    }

    public Game acquireGame(GameType type) {
        GameContainer pool = this.games.get(type);

        return pool != null ? pool.acquire(false) : null;
    }

    public Game getGame(final PlayerCurrentGame currentGame) {
        if(currentGame == null) return null;

        final GameContainer pool = this.games.get(currentGame.type());

        if(pool == null) return null;

        return pool.getGame(currentGame.gameId());
    }
}
