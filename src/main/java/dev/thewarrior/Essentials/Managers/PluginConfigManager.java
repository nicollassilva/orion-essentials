package dev.thewarrior.Essentials.Managers;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.Essentials.Managers.Data.PluginConfigData;
import dev.thewarrior.Essentials.Utils.Location;
import dev.thewarrior.Essentials.Utils.Logger;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class PluginConfigManager extends StorableManager<PluginConfigData> {
    public static Location SPAWN_LOCATION = null;

    public PluginConfigManager(@Nonnull Path dataFolder) {
        super(dataFolder, "config.json", PluginConfigData.class);
    }

    @Override
    protected PluginConfigData createDefaultData() {
        return new PluginConfigData();
    }

    protected void onDataLoaded() {
        Logger.info("Plugin configuration loaded");

        SPAWN_LOCATION = this.getSpawnLocation();
    }

    public String getDiscordLink() {
        return this.data.getDiscordLink();
    }

    public CompletableFuture<Void> setDiscordLink(String discordLink) {
        this.data.setDiscordLink(discordLink);

        return this.saveConfig();
    }

    public int getTeleportDelaySeconds() {
        return this.data.getTeleportDelaySeconds();
    }

    public Location getSpawnLocation() {
        return this.data.getSpawnLocation();
    }

    public CompletableFuture<Void> setSpawnLocation(Location location) {
        this.data.setSpawnLocation(location);
        SPAWN_LOCATION = location;

        return this.saveConfig();
    }

    public String getBroadcastFormat() {
        return this.data.getBroadcastFormat();
    }

    public String getChatFormat() {
        return this.data.getChatFormat();
    }

    /**
     * Syncs the saved spawn with the world's native spawn provider.
     * This updates the spawn marker on the map.
     * Should be called after worlds are loaded.
     */
    public void syncWorldSpawnProvider() {
        Location spawn = this.getSpawnLocation();
        if (spawn == null) {
            return;
        }

        World world = Universe.get().getWorld(spawn.getWorld());
        if (world == null) {
            Logger.warning("Could not sync spawn provider: world '" + spawn.getWorld() + "' not found");
            return;
        }

        Vector3d position = new Vector3d(spawn.getX(), spawn.getY(), spawn.getZ());
        Vector3f rotation = new Vector3f(0, spawn.getYaw(), 0);
        Transform spawnTransform = new Transform(position, rotation);

        world.getWorldConfig().setSpawnProvider(new GlobalSpawnProvider(spawnTransform));

        Logger.info("Synced spawn provider for world '" + spawn.getWorld() + "'");
    }
}
