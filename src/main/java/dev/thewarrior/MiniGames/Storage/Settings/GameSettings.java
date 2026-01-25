package dev.thewarrior.MiniGames.Storage.Settings;

import java.util.List;

public class GameSettings {
    private String name;
    private String description;
    private boolean isEnabled;

    private int maxPlayersPerGame;
    private int minPlayersToStart;

    private int minDuration;
    private int maxDuration;

    private int countdownBeforeStart;
    private int countdownAfterEnd;

    private List<String> permissionsRequired;

    public GameSettings() {}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return isEnabled;
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

    public int getMinDuration() {
        return minDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public List<String> getPermissionsRequired() {
        return permissionsRequired;
    }
}
