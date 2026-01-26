package dev.thewarrior.MiniGames.Storage;

import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.MiniGames.Storage.Prefabs.GamesPrefabsData;
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
}
