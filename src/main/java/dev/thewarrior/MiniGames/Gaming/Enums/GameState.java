package dev.thewarrior.MiniGames.Gaming.Enums;

public enum GameState {
    CREATING("Arena is being created"),
    FREE("Can be used (no games found for this)"),
    WAITING("Wait for min. players to start"),
    STARTING("Countdown to enter in the arena"),
    COUNTDOWN("Countdown to starting the game (if needs)"),
    RUNNING("Game is running"),
    ENDING("Time to send rewards and send all to lobby"),
    ENDED("The match is completed, reset arena"),
    DISPOSING("Can be disposed"),

    ;

    private final String description;

    GameState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canJoin() {
        return this == CREATING || this == FREE || this == WAITING || this == STARTING;
    }

    public boolean isQueuedTick() {
        return this.canJoin() || this == COUNTDOWN;
    }

    public boolean isInGame() {
        return this == COUNTDOWN || this == RUNNING;
    }
}
