package dev.thewarrior.MiniGames.Gaming.Container;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import dev.thewarrior.MiniGames.Storage.Prefabs.GamePrefab;

public record GameArena(GamePrefab prefab, Vector3i position) {
    public Vector3d getRespawnPosition(Vector3d position) {
        return position.add(this.position);
    }
}
