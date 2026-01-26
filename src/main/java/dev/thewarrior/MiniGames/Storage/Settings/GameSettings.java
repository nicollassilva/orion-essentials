package dev.thewarrior.MiniGames.Storage.Settings;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.List;

public class GameSettings {
    private String name;
    private String type;
    private String description;
    private String worldName;

    private boolean isEnabled;

    private int maxPlayersPerGame;
    private int minPlayersToStart;

    private int minDuration;
    private int maxDuration;

    private int countdownBeforeStart;
    private int countdownAfterEnd;

    private int maxInstances;
    private int maxArenaSize;

    private List<String> permissionsRequired;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isDisabled() {
        return !this.isEnabled;
    }

    public int getMaxPlayersPerGame() {
        return maxPlayersPerGame;
    }

    public int getMinPlayersToStart() {
        return minPlayersToStart;
    }

    public int getCountdownBeforeStart() {
        return countdownBeforeStart;
    }

    public int getCountdownAfterEnd() {
        return countdownAfterEnd;
    }

    public int getMaxInstances() {
        return maxInstances;
    }

    public int getMaxArenaSize() {
        return maxArenaSize;
    }

    public int getMinDuration() {
        return minDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public List<String> getPermissionsRequired() {
        return permissionsRequired;
    }

    public boolean isType(final GameType gameType) {
        return this.type.equalsIgnoreCase(gameType.name());
    }
}
