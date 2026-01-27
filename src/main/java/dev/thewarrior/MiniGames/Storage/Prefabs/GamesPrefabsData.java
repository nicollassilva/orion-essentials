package dev.thewarrior.MiniGames.Storage.Prefabs;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.OrionBootstrap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class GamesPrefabsData {
    private List<GamePrefab> settings = new CopyOnWriteArrayList<>();
    private transient Map<GameType, List<GamePrefab>> cachedPrefabsByType;

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

    public GamePrefab getRandomPrefabByType(final GameType gameType) {
        if(gameType == null) return null;

        if(this.cachedPrefabsByType == null) {
            this.cachedPrefabsByType = new ConcurrentHashMap<>();
        }

        List<GamePrefab> prefabsOfType = this.cachedPrefabsByType.get(gameType);

        if(prefabsOfType == null) {
            prefabsOfType = new CopyOnWriteArrayList<>();

            for (final GamePrefab setting : this.settings) {
                if (setting.isType(gameType)) {
                    prefabsOfType.add(setting);
                }
            }

            this.cachedPrefabsByType.put(gameType, prefabsOfType);
        }

        if(prefabsOfType.isEmpty()) {
            return null;
        }

        final int random = OrionBootstrap.random.nextInt(prefabsOfType.size());

        if(random >= prefabsOfType.size()) {
            return null;
        }

        final GamePrefab prefab = prefabsOfType.get(random);

        if(prefab != null && (prefab.getGameSpawnsData() == null || prefab.getGameSpawnsData().isEmpty())) return null;

        return prefab;
    }
}
