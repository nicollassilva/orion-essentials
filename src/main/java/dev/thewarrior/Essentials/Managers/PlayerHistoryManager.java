package dev.thewarrior.Essentials.Managers;

import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.Essentials.Managers.Data.Config.PlayerHistoryData;
import dev.thewarrior.Essentials.Utils.Logger;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerHistoryManager extends StorableManager<PlayerHistoryData> {
    public PlayerHistoryManager(@Nonnull Path dataFolder) {
        super(dataFolder, "player_history.json", PlayerHistoryData.class);
    }

    protected void onDataLoaded() {
        Logger.info("Player history data loaded. Total entries: " + this.data.getHistory().size());
    }

    @Override
    protected PlayerHistoryData createDefaultData() {
        return new PlayerHistoryData();
    }

    public boolean hasHistory(UUID playerUUID) {
        return this.data.containsHistory(playerUUID);
    }

    public void addHistory(UUID playerUUID) {
        this.data.addHistory(playerUUID);

        this.saveConfig();
    }
}
