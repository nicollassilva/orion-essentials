package dev.thewarrior.MiniGames.Gaming.Container;

import com.hypixel.hytale.math.vector.Vector3i;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Storage.GamesPrefabsStorage;
import dev.thewarrior.MiniGames.Storage.GamesSettingsStorage;
import dev.thewarrior.MiniGames.Storage.Prefabs.GamePrefab;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;
import dev.thewarrior.MiniGames.World.WorldManager;

public class GameArenaFactory {
    public static GameArenaFactory instance;

    private final GamesSettingsStorage settingsStorage;
    private final GamesPrefabsStorage prefabsStorage;
    private final WorldManager worldManager;

    public GameArenaFactory(final GamesSettingsStorage settingsStorage, final GamesPrefabsStorage prefabsStorage, final WorldManager worldManager) {
        this.settingsStorage = settingsStorage;
        this.prefabsStorage = prefabsStorage;
        this.worldManager = worldManager;
    }

    public static void init(final GamesSettingsStorage settingsStorage, final GamesPrefabsStorage prefabsStorage, final WorldManager worldManager) {
        if (instance == null) {
            instance = new GameArenaFactory(settingsStorage, prefabsStorage, worldManager);
        }
    }

    public static GameArenaFactory get() {
        if(instance == null) {
            throw new IllegalStateException("GameArenaFactory is not initialized yet.");
        }

        return instance;
    }

    public GameArena createArena(GameType type) {
        if(type == null) {
            Logger.warning("[GameArenaFactory] GameArena type is null.");
            return null;
        }

        final GameSettings settings = this.settingsStorage.getByType(type);

        if(settings == null || settings.isDisabled()) {
            Logger.warning("[GameArenaFactory] GameArena type " + type + " is disabled.");
            return null;
        }

        final Vector3i arenaCenterPosition = this.worldManager.generateCenterPositionForGame(settings);

        if(arenaCenterPosition == null || arenaCenterPosition == Vector3i.ZERO) {
            Logger.warning("[GameArenaFactory] Failed to generate center position for GameArena type " + type + ".");
            return null;
        }

        final GamePrefab randomArena = this.prefabsStorage.getRandomGameArena(settings);

        if(randomArena == null) return null;

        return new GameArena(randomArena, arenaCenterPosition);
    }
}
