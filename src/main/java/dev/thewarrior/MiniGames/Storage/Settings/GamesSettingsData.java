package dev.thewarrior.MiniGames.Storage.Settings;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class GamesSettingsData {
    private List<GameSettings> settings = new CopyOnWriteArrayList<>();

    public GameSettings getGameSettings(final Predicate<GameSettings> filter) {
        for (final GameSettings setting : this.settings) {
            if (filter.test(setting)) {
                return setting;
            }
        }

        return null;
    }

    public GameSettings getByType(final GameType gameType) {
        return this.getGameSettings(setting -> setting.isType(gameType));
    }

    public GameSettings getByName(final String name) {
        return this.getGameSettings(setting -> setting.getName().equalsIgnoreCase(name));
    }
}
