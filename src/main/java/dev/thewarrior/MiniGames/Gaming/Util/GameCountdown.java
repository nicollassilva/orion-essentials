package dev.thewarrior.MiniGames.Gaming.Util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GameCountdown {
    private final AtomicInteger remaining;
    private final int initial;
    private final Consumer<Integer> onTick;
    private final Runnable onComplete;
    private volatile boolean cancelled;

    public GameCountdown(int seconds, Consumer<Integer> onTick, Runnable onComplete) {
        this.initial = seconds;
        this.remaining = new AtomicInteger(seconds);
        this.onTick = onTick;
        this.onComplete = onComplete;
    }

    public boolean tick() {
        if (cancelled) return true;

        int current = remaining.decrementAndGet();

        if (current <= 0) {
            onComplete.run();
            return true;
        }

        onTick.accept(current);
        return false;
    }

    public void cancel() {
        cancelled = true;
    }

    public void reset() {
        remaining.set(initial);
        cancelled = false;
    }

    public int getRemaining() {
        return remaining.get();
    }

    public boolean isCancelled() {
        return cancelled;
    }
}

