package dev.thewarrior.MiniGames.Gaming.Arena;

import com.hypixel.hytale.math.vector.Vector3d;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.List;

public class ArenaConfig {
    private final String id;
    private final String name;
    private final GameType gameType;
    private final String worldTemplate;
    private final Vector3d lobbySpawn;
    private final List<Vector3d> gameSpawns;
    private final List<Vector3d> spectatorSpawns;

    private ArenaConfig(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.gameType = builder.gameType;
        this.worldTemplate = builder.worldTemplate;
        this.lobbySpawn = builder.lobbySpawn;
        this.gameSpawns = List.copyOf(builder.gameSpawns);
        this.spectatorSpawns = List.copyOf(builder.spectatorSpawns);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getWorldTemplate() {
        return worldTemplate;
    }

    public Vector3d getLobbySpawn() {
        return lobbySpawn;
    }

    public List<Vector3d> getGameSpawns() {
        return gameSpawns;
    }

    public List<Vector3d> getSpectatorSpawns() {
        return spectatorSpawns;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private GameType gameType;
        private String worldTemplate;
        private Vector3d lobbySpawn;
        private List<Vector3d> gameSpawns = List.of();
        private List<Vector3d> spectatorSpawns = List.of();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder gameType(GameType gameType) {
            this.gameType = gameType;
            return this;
        }

        public Builder worldTemplate(String worldTemplate) {
            this.worldTemplate = worldTemplate;
            return this;
        }

        public Builder lobbySpawn(Vector3d spawn) {
            this.lobbySpawn = spawn;
            return this;
        }

        public Builder gameSpawns(List<Vector3d> spawns) {
            this.gameSpawns = spawns;
            return this;
        }

        public Builder spectatorSpawns(List<Vector3d> spawns) {
            this.spectatorSpawns = spawns;
            return this;
        }

        public ArenaConfig build() {
            return new ArenaConfig(this);
        }
    }
}

