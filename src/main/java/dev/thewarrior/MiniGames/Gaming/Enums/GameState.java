package dev.thewarrior.MiniGames.Gaming.Enums;

public enum GameState {
    WAITING,
    STARTING,
    RUNNING,
    ENDING;

    public boolean canJoin() {
        return this == WAITING || this == STARTING;
    }

    public boolean isActive() {
        return this == RUNNING;
    }
}
