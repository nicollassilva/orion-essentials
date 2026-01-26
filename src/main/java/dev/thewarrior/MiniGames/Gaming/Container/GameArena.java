package dev.thewarrior.MiniGames.Gaming.Container;

import com.hypixel.hytale.math.vector.Vector3d;
import dev.thewarrior.MiniGames.Storage.Prefabs.GamePrefab;

public class GameArena {
    private final GamePrefab prefab;
    private final Vector3d position;

    public GameArena(GamePrefab prefab, Vector3d position) {
        this.prefab = prefab;
        this.position = position;
    }

    public GamePrefab getPrefab() {
        return prefab;
    }

    public Vector3d getPosition() {
        return position;
    }
}
