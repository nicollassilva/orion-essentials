package dev.thewarrior.MiniGames.Gaming.Arena;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

import java.util.EnumMap;
import java.util.Map;

public class GameArenaManager {
    private final Map<GameType, GameArenaPool> pools;

    public GameArenaManager(final GamesSettingsStorage settingsStorage) {
        this.pools = new EnumMap<>(GameType.class);

        for (GameType type : GameType.values()) {
            final GameSettings settings = settingsStorage.getByType(type);

            if(settings == null || settings.isDisabled()) continue;

            this.pools.put(type, new GameArenaPool(type, settings));
        }
    }

    public Game acquireArena(GameType type) {
        GameArenaPool pool = this.pools.get(type);

        return pool != null ? pool.acquire() : null;
    }
}
