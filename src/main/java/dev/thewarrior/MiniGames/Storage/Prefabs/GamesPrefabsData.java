package dev.thewarrior.MiniGames.Storage.Prefabs;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class GamesPrefabsData {
    private List<GamePrefab> settings = new CopyOnWriteArrayList<>();

    public GamePrefab getPrefab(final Predicate<GamePrefab> filter) {
        for (final GamePrefab setting : this.settings) {
            if (filter.test(setting)) {
                return setting;
            }
        }

        return null;
    }

    public GamePrefab getByType(final GameType gameType) {
        return this.getPrefab(setting -> setting.isType(gameType));
    }

    public GamePrefab getByName(final String name) {
        return this.getPrefab(setting -> setting.getName().equalsIgnoreCase(name));
    }
}
