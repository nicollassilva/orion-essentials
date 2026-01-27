package dev.thewarrior.MiniGames.Storage.Prefabs;

import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.List;
import java.util.Set;

public class GamePrefab {
    private String name;
    private Set<String> types;
    private boolean isEnabled;
    private GamePrefabSpawnData lobbySpawnData;
    private List<GamePrefabSpawnData> gameSpawnsData;
    private transient BlockSelection gameBuild;

    public String getName() {
        return name;
    }

    public boolean isDisabled() {
        return !this.isEnabled;
    }

    public boolean isType(final GameType type) {
        return this.types.contains(type.name().toLowerCase());
    }

    public GamePrefabSpawnData getLobbySpawnData() {
        return lobbySpawnData;
    }

    public List<GamePrefabSpawnData> getGameSpawnsData() {
        return gameSpawnsData;
    }

    public BlockSelection getGameBuild() {
        return gameBuild;
    }

    public void setGameBuild(final BlockSelection gameBuild) {
        this.gameBuild = gameBuild;
    }
}
