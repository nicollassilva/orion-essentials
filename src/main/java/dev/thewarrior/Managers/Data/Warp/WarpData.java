package dev.thewarrior.Managers.Data.Warp;

import dev.thewarrior.Utils.Location;

import java.util.HashMap;
import java.util.Map;

public class WarpData {
    private final WarpConfig config = new WarpConfig();
    private final Map<String, Location> warps = new HashMap<>();

    public Map<String, Location> getWarps() {
        return this.warps;
    }

    public Location getWarp(String name) {
        return this.warps.get(name.toLowerCase());
    }

    public void setWarp(String name, Location location) {
        this.warps.put(name.toLowerCase(), location);
    }

    public boolean deleteWarp(String name) {
        return this.warps.remove(name.toLowerCase()) != null;
    }

    public boolean hasWarp(String name) {
        return this.warps.containsKey(name.toLowerCase());
    }

    public WarpConfig getWarpConfig() {
        return this.config;
    }
}
