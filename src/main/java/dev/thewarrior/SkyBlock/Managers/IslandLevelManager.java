package dev.thewarrior.SkyBlock.Managers;

import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelSettings;

import java.nio.file.Path;

public class IslandLevelManager extends StorableManager<IslandLevelSettings> {
    public IslandLevelManager(Path dataFolder) {
        super(dataFolder, "levels.json", IslandLevelSettings.class);
    }

    @Override
    public IslandLevelSettings createDefaultData() {
        return new IslandLevelSettings();
    }
}
