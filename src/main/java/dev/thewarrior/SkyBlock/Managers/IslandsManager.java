package dev.thewarrior.SkyBlock.Managers;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.Essentials.Utils.ThrottledTask;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandsData;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class IslandsManager extends StorableManager<IslandsData> {
    private final SkyBlockSettingsManager skyBlockSettingsManager;

    private final ThrottledTask saveTask;

    private final Set<IslandData> islandsToSave;

    public IslandsManager(Path dataFolder, final SkyBlockSettingsManager skyBlockSettingsManager) {
        super(dataFolder, "islands.json", IslandsData.class);

        this.islandsToSave = ConcurrentHashMap.newKeySet();
        this.skyBlockSettingsManager = skyBlockSettingsManager;

        this.saveTask = new ThrottledTask(HytaleServer.SCHEDULED_EXECUTOR, this::saveData, 5);
    }

    public IslandsData createDefaultData() {
        return new IslandsData();
    }

    public IslandData createIslandForPlayer(final PlayerRef ref, String worldName) {
        final UUID ownerId = ref.getUuid();

        if(UUIDUtil.isEmptyOrNull(ownerId)) {
            throw new IllegalArgumentException("Cannot create island for null or empty UUID");
        }

        if(worldName == null || worldName.isEmpty()) {
            throw new IllegalStateException("Generated world name is null or empty");
        }

        final IslandData islandData = new IslandData(ref.getUuid(), worldName);

        islandData.setEnterTitle("Bem vindo(a) à ilha de " + ref.getUsername());

        this.data.add(islandData);

        this.save(islandData);

        return islandData;
    }

    public int getCurrentWorldCountForPlayer(final PlayerRef ref) {
        final UUID ownerId = ref.getUuid();

        if(UUIDUtil.isEmptyOrNull(ownerId)) {
            throw new IllegalArgumentException("Cannot get world count for null or empty UUID");
        }

        return this.data.getIslandCountForPlayer(ref);
    }

    public IslandData getIslandByWorldName(final String id) {
        return this.data.getIslandByWorldName(id);
    }

    public IslandData getIslandByNameAndOwner(final String islandName, final UUID ownerName) {
        return this.data.getIslandByNameAndOwner(islandName, ownerName);
    }

    public void save(final IslandData data) {
        this.islandsToSave.add(data);

        this.saveTask.execute();
    }

    private void saveData() {
        final Set<IslandData> groupsToProcess = new ObjectArraySet<>();

        this.islandsToSave.removeIf(group -> {
            groupsToProcess.add(group);

            return true;
        });

        CompletableFuture.runAsync(() -> {
            boolean needsUpdate = false;

            for(final IslandData islandData : groupsToProcess) {
                needsUpdate |= islandData.needsUpdate();
            }

            if(needsUpdate) {
                this.saveConfig();
            }
        });
    }
}
