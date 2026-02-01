package dev.thewarrior.SkyBlock.Managers.SkyBlock;

import com.hypixel.hytale.math.vector.Vector3d;

public class SkyBlockSettingsData {
    private String prefabName = "island_default.prefab.json";

    private Vector3d spawnLocation = new Vector3d(2.5, 109, -2.6);
    private Vector3d spawnRotation = new Vector3d(0, -3.12, 0);

    private SkyBlockSettings settings = new SkyBlockSettings();

    public SkyBlockSettings getSettings() {
        return settings;
    }

    public Vector3d getSpawnLocation() {
        return spawnLocation;
    }

    public Vector3d getSpawnRotation() {
        return spawnRotation;
    }

    public String getPrefabName() {
        return prefabName;
    }
}
