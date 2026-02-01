package dev.thewarrior.SkyBlock.Managers.SkyBlock;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public class SkyBlockSettings {
    private String worldPrefix = "island_{uuid}_{number}";

    private Object2ObjectArrayMap<String, Integer> defaultIslandSizes = new Object2ObjectArrayMap<>() {{
        put("tiny", 100);
        put("small", 300);
        put("medium", 500);
        put("large", 1000);
        put("king", 2500);
    }};

    private Object2ObjectArrayMap<String, Integer> maxIslandsPerPlayer = new Object2ObjectArrayMap<>() {{
        put("default", 1);
        put("vip", 2);
        put("vip2", 3);
        put("vip3", 5);
    }};

    private Object2ObjectArrayMap<String, Integer> maxFriendsPerIsland = new Object2ObjectArrayMap<>() {{
        put("default", 3);
        put("vip", 5);
        put("vip2", 7);
        put("vip3", 10);
    }};

    public String getWorldPrefix() {
        return this.worldPrefix;
    }

    public Object2ObjectArrayMap<String, Integer> getDefaultIslandSizes() {
        return this.defaultIslandSizes;
    }

    public Object2ObjectArrayMap<String, Integer> getMaxIslandsPerPlayer() {
        return this.maxIslandsPerPlayer;
    }

    public Object2ObjectArrayMap<String, Integer> getMaxFriendsPerIsland() {
        return this.maxFriendsPerIsland;
    }
}
