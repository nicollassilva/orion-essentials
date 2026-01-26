package dev.thewarrior.MiniGames.Gaming.Util;

import java.util.concurrent.atomic.AtomicLong;

public class GameTimer {
    private final AtomicLong startTime;
    private final long durationMs;
    private volatile boolean paused;
    private volatile long pausedAt;

    public GameTimer(long durationSeconds) {
        this.durationMs = durationSeconds * 1000;
        this.startTime = new AtomicLong(0);
    }

    public void start() {
        startTime.set(System.currentTimeMillis());
        paused = false;
    }

    public void pause() {
        if (!paused) {
            pausedAt = System.currentTimeMillis();
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            long pauseDuration = System.currentTimeMillis() - pausedAt;
            startTime.addAndGet(pauseDuration);
            paused = false;
        }
    }

    public long getElapsedMs() {
        if (!isStarted()) return 0;
        if (paused) return pausedAt - startTime.get();
        return System.currentTimeMillis() - startTime.get();
    }

    public long getRemainingMs() {
        long elapsed = getElapsedMs();
        return Math.max(0, durationMs - elapsed);
    }

    public int getElapsedSeconds() {
        return (int) (getElapsedMs() / 1000);
    }

    public int getRemainingSeconds() {
        return (int) (getRemainingMs() / 1000);
    }

    public boolean isExpired() {
        return isStarted() && getRemainingMs() <= 0;
    }

    public boolean isStarted() {
        return startTime.get() > 0;
    }

    public boolean isPaused() {
        return paused;
    }

    public void reset() {
        startTime.set(0);
        paused = false;
    }
}

