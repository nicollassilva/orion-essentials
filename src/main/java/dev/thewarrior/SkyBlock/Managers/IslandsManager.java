package dev.thewarrior.SkyBlock.Managers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ClientEffectWorldSettings;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.FlatWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.Essentials.Utils.TeleportUtil;
import dev.thewarrior.Essentials.Utils.ThrottledTask;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandsData;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.nio.file.Path;
import java.util.List;
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

    public IslandData createIslandForPlayer(final PlayerRef ref, String worldName, Vector3d spawnLocation, Vector3d spawnRotation) {
        final UUID ownerId = ref.getUuid();

        if(UUIDUtil.isEmptyOrNull(ownerId)) {
            throw new IllegalArgumentException("Cannot create island for null or empty UUID");
        }

        if(worldName == null || worldName.isEmpty()) {
            throw new IllegalStateException("Generated world name is null or empty");
        }

        final IslandData islandData = new IslandData(ref.getUuid(), worldName, spawnLocation, spawnRotation);

        islandData.setEnterTitle("Bem vindo(a) à ilha de " + ref.getUsername());

        this.data.add(islandData);

        this.save(islandData);

        return islandData;
    }

    public CompletableFuture<IslandData> createIslandForPlayerAsync(
            final PlayerRef playerRef,
            final Store<EntityStore> store,
            final Ref<EntityStore> ref,
            final Runnable onSuccess,
            final Runnable onFailure
    ) {
        final Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null || player.wasRemoved()) {
            if (onFailure != null) onFailure.run();

            return CompletableFuture.completedFuture(null);
        }

        final List<IslandData> islands = this.getData().getIslandsForPlayer(playerRef);

        if (this.skyBlockSettingsManager.isPlayerBlockedToCreateIsland(player, islands)) {
            if (onFailure != null) onFailure.run();

            return CompletableFuture.completedFuture(null);
        }

        final String worldName = this.skyBlockSettingsManager.getNewWorldNameForPlayer(
                playerRef, this.getCurrentWorldCountForPlayer(playerRef) + 1
        );

        if (worldName == null || worldName.isEmpty()) {
            if (onFailure != null) onFailure.run();

            return CompletableFuture.completedFuture(null);
        }

        final Vector3d spawnLocation = this.skyBlockSettingsManager.getDefaultIslandSpawnLocation();
        final Vector3d spawnRotation = this.skyBlockSettingsManager.getDefaultIslandSpawnRotation();

        final BlockSelection defaultPrefabSelection = PrefabStore.get().getServerPrefab(this.skyBlockSettingsManager.getDefaultPrefabName());

        if (defaultPrefabSelection == null) {
            if (onFailure != null) onFailure.run();

            return CompletableFuture.completedFuture(null);
        }

        return Universe.get().makeWorld(worldName, Constants.UNIVERSE_PATH.resolve("worlds").resolve(worldName), this.getIslandWorldConfig(spawnLocation))
                .thenApply(createdWorld -> {
                    boolean hasErrors = true;

                    try {
                        final IslandData island = this.createIslandForPlayer(playerRef, worldName, spawnLocation, spawnRotation);

                        if (island != null) {
                            hasErrors = false;

                            createdWorld.execute(() -> {
                                defaultPrefabSelection.place(null, createdWorld, new Vector3i(0, 100, 0), null);

                                TeleportUtil.teleport(playerRef, store, ref, worldName, spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), (float) spawnRotation.getY(), 0);
                            });

                            if (onSuccess != null) onSuccess.run();
                            return island;
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }

                    if (hasErrors) {
                        Universe.get().removeWorld(worldName);

                        if (onFailure != null) onFailure.run();
                    }

                    return null;
                })
                .exceptionally((e) -> {
                    e.printStackTrace();

                    if (onFailure != null) onFailure.run();

                    return null;
                });
    }

    private WorldConfig getIslandWorldConfig(Vector3d spawn) {
        WorldConfig config = new WorldConfig();

        config.setUuid(UUID.randomUUID());
        config.setForcedWeather("Zone1_Sunny");
        config.setSpawningNPC(false);
        config.setSpawnProvider(new GlobalSpawnProvider(new Transform(spawn)));
        config.setGameTime(java.time.Instant.parse("0001-01-01T12:00:00Z"));

        config.setClientEffects(ClientEffectWorldSettings.CODEC.getDefaultValue());

        config.setWorldGenProvider(new VoidWorldGenProvider(FlatWorldGenProvider.DEFAULT_TINT, "Env_Zone1_Plains"));
        return config;
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

    public IslandData getFirstIslandForOwner(final UUID ownerId) {
        return this.data.getFirstIslandForOwner(ownerId);
    }

    public List<IslandData> getIslandsForPlayer(final UUID ownerId) {
        return this.data.getIslandsForPlayer(ownerId);
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
                // TODO: Optimize to only save changed islands
                this.saveConfig();
            }
        });
    }
}
