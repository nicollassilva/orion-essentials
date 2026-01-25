package dev.thewarrior.Essentials.Managers;

import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.Essentials.Managers.Data.Config.PlayerHistoryData;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerHistoryManager extends StorableManager<PlayerHistoryData> {
    public PlayerHistoryManager(@Nonnull Path dataFolder) {
        super(dataFolder, "player_history.json", PlayerHistoryData.class);
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
