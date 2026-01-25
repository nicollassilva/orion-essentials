package dev.thewarrior.MiniGames.World;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ClientEffectWorldSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;
import dev.thewarrior.Essentials.Utils.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class WorldManager {
    private Instant defaultWorldTime = LocalDate.now(ZoneId.of("America/Sao_Paulo"))
            .atTime(12, 0)
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .toInstant();

    public void start() {
        this.checkCommonMinigamesWorldExistence();
    }

    private void checkCommonMinigamesWorldExistence() {
        World worldExists = Universe.get().getWorld("minigames_common");

        if(worldExists != null) {
            Logger.info("The world 'minigames_common' exists. Deleting and recreating...");
            World fallback = Universe.get().getWorld("default");
            if(fallback != null)  worldExists.drainPlayersTo(fallback);
            Universe.get().removeWorld("minigames_common");
        } else {
            Logger.info("The world 'minigames_common' does not exist. Creating...");
        }

        CompletableFutureUtil._catch(
                Universe.get().makeWorld("minigames_common", Universe.getWorldGenPath(), this.setupWorldConfig())
                        .thenRun(() -> {
                            World createdWorld = Universe.get().getWorld("minigames_common");

                            if (createdWorld == null) {
                                Logger.error("Failed to create 'minigames_common' world: World is null after creation.");
                                HytaleServer.get().shutdownServer(ShutdownReason.CRASH);
                                return;
                            }

                            createdWorld.execute(() -> {
                                BlockSelection prefab = PrefabStore.get().getServerPrefab("TntRun_V1.prefab.json");

                                prefab.place(null, createdWorld, new Vector3i(0, 1, 0), null);
                            });
                        })
                        .exceptionally(
                                throwable -> {
                                    Logger.error("Failed to add 'minigames_common' world", throwable);
                                    return null;
                                }
                        )
        );
    }

    private WorldConfig setupWorldConfig() {
        WorldConfig config = new WorldConfig();

        config.setIsSpawnMarkersEnabled(false);
        config.setObjectiveMarkersEnabled(false);
        config.setDeleteOnRemove(true);
        config.setUuid(UUID.randomUUID());
        config.setSavingPlayers(false);
        config.setCanSaveChunks(false);
        config.setTicking(true);
        config.setBlockTicking(true);
        config.setGameTimePaused(true);
        config.setSpawningNPC(false);
        config.setForcedWeather("Zone1_Sunny");
        config.setSpawnProvider(new GlobalSpawnProvider(new Transform(0, 5, 0)));
        config.setGameTime(this.defaultWorldTime);

        config.setClientEffects(ClientEffectWorldSettings.CODEC.getDefaultValue());

        config.setWorldGenProvider(new VoidWorldGenProvider(new Color((byte) 91, (byte) -98, (byte) 40), "Env_Zone1_Plains"));

        return config;
    }
}
