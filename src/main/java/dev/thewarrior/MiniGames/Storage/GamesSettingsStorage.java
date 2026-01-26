package dev.thewarrior.MiniGames.Storage;

import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;
import dev.thewarrior.MiniGames.Storage.Settings.GamesSettingsData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.nio.file.Path;

public class GamesSettingsStorage extends StorableManager<GamesSettingsData> {
    public GamesSettingsStorage(@NonNullDecl Path dataFolder) {
        super(dataFolder, "minigames.json", GamesSettingsData.class);
    }

    @Override
    protected GamesSettingsData createDefaultData() {
        return new GamesSettingsData();
    }

    public GameSettings getByType(final GameType gameType) {
        return this.data.getByType(gameType);
    }
}
