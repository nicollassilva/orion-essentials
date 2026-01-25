package dev.thewarrior.Essentials.Managers;

import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.Essentials.Managers.Data.Warp.WarpData;
import dev.thewarrior.Essentials.Utils.Enums.WarpValidationError;
import dev.thewarrior.Essentials.Utils.Location;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class WarpManager extends StorableManager<WarpData> {
    public static final Pattern WARP_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    public WarpManager(@Nonnull Path dataFolder) {
        super(dataFolder, "warps.json", WarpData.class);
    }

    @Override
    protected WarpData createDefaultData() {
        return new WarpData();
    }

    public CompletableFuture<WarpValidationError> setWarp(String name, Location location) {
        WarpValidationError error = WarpValidationError.validateWarpName(name);

        if (error != WarpValidationError.NONE) {
            return CompletableFuture.completedFuture(error);
        }

        this.data.setWarp(name.toLowerCase(), location);
        return this.saveConfig().thenApply(v -> WarpValidationError.NONE);
    }

    public CompletableFuture<Boolean> deleteWarp(String name) {
        boolean removed = this.data.deleteWarp(name.toLowerCase());

        if (removed) {
            return this.saveConfig().thenApply(v -> true);
        }

        return CompletableFuture.completedFuture(false);
    }

    public Location getWarp(String name) {
        return this.data.getWarp(name.toLowerCase());
    }

    public Map<String, Location> getWarps() {
        return this.data.getWarps();
    }

    public boolean hasWarp(String name) {
        return this.data.hasWarp(name.toLowerCase());
    }
}

