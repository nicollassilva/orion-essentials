package dev.thewarrior.MiniGames.Gaming.Container;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
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

    public Game acquireGame(GameType type) {
        GameContainer pool = this.games.get(type);

        return pool != null ? pool.acquire() : null;
    }
}
