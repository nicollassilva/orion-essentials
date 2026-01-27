package dev.thewarrior.MiniGames.Storage;

import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.MiniGames.Storage.Prefabs.GamePrefab;
import dev.thewarrior.MiniGames.Storage.Prefabs.GamesPrefabsData;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.nio.file.Path;

public class GamesPrefabsStorage extends StorableManager<GamesPrefabsData> {
    public GamesPrefabsStorage(@NonNullDecl Path dataFolder) {
        super(dataFolder, "prefabs.json", GamesPrefabsData.class);
    }

    @Override
    protected GamesPrefabsData createDefaultData() {
        return new GamesPrefabsData();
    }

    public GamePrefab getRandomGameArena(final GameSettings settings) {
        if(settings == null) {
            Logger.warning("[GamesPrefabsStorage] GameSettings is null.");
            return null;
        }

        final GamesPrefabsData data = this.getData();

        if(data == null) {
            Logger.warning("[GamesPrefabsStorage] GamesPrefabsData is null.");
            return null;
        }

        try {
            return data.getRandomPrefabByType(settings.getType());
        } catch (Exception e) {
            Logger.warning("[GamesPrefabsStorage] Failed to get random GamePrefab for GameType " + settings.getType() + ": " + e.getMessage());
            return null;
        }
    }
}
