package dev.thewarrior.MiniGames.World;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ClientEffectWorldSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.FlatWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {
    private final Instant defaultWorldTime = LocalDate.now(ZoneId.of("America/Sao_Paulo"))
            .atTime(12, 0)
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .toInstant();

    private final Map<String, World> minigameWorlds;

    private final WorldTerrainManager worldTerrainManager;

    public WorldManager() {
        this.minigameWorlds = new ConcurrentHashMap<>();
        this.worldTerrainManager = new WorldTerrainManager();
    }

    public void start() {

    }

    public CompletableFuture<World> getWorldForGame(GameSettings gameSettings) {
        if(gameSettings == null || gameSettings.getWorldName() == null) {
            Logger.error("GameSettings or WorldName is null. Cannot retrieve world.");
            return null;
        }

        final String worldName = gameSettings.getWorldName();

        if(worldName.isEmpty()) {
            Logger.error("WorldName is empty in GameSettings. Cannot retrieve world.");
            return null;
        }

        final World world = this.minigameWorlds.getOrDefault(worldName, null);

        if(world == null) {
            Logger.error(String.format("World '%s' is not loaded. Trying to create or fetch it.", worldName));
        }

        return Universe.get().makeWorld(worldName, Universe.getWorldGenPath(), this.setupWorldConfig());
    }

    public WorldTerrainManager getWorldTerrainManager() {
        return this.worldTerrainManager;
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

        config.setWorldGenProvider(new VoidWorldGenProvider(FlatWorldGenProvider.DEFAULT_TINT, "Env_Zone1_Plains"));

        return config;
    }
}
