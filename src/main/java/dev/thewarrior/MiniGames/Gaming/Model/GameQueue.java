package dev.thewarrior.MiniGames.Gaming.Model;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GameQueue {
    private final GameType type;
    private final Queue<QueuedPlayer> queue;
    private final AtomicInteger size;

    public GameQueue(GameType type) {
        this.type = type;
        this.queue = new ConcurrentLinkedQueue<>();
        this.size = new AtomicInteger(0);
    }

    public boolean enqueue(QueuedPlayer player) {
        if (queue.offer(player)) {
            size.incrementAndGet();
            return true;
        }
        return false;
    }

    public QueuedPlayer poll() {
        QueuedPlayer player = queue.poll();
        if (player != null) {
            size.decrementAndGet();
        }
        return player;
    }

    public boolean remove(QueuedPlayer player) {
        if (queue.remove(player)) {
            size.decrementAndGet();
            return true;
        }
        return false;
    }

    public int size() {
        return size.get();
    }

    public boolean hasEnoughPlayers() {
        return size.get() >= type.getMinPlayers();
    }

    public GameType getType() {
        return type;
    }
}

