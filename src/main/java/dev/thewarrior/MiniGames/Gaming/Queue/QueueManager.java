package dev.thewarrior.MiniGames.Gaming.Queue;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.GameQueue;
import dev.thewarrior.MiniGames.Gaming.Model.QueuedPlayer;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {
    private final Map<GameType, GameQueue> queues;
    private final Map<UUID, QueuedPlayer> playerQueue; // player -> fila atual

    public QueueManager() {
        this.queues = new EnumMap<>(GameType.class);
        this.playerQueue = new ConcurrentHashMap<>();

        for (GameType type : GameType.values()) {
            queues.put(type, new GameQueue(type));
        }
    }

    public boolean addToQueue(UUID playerId, GameType type) {
        if (playerQueue.containsKey(playerId)) {
            return false;
        }

        QueuedPlayer queuedPlayer = new QueuedPlayer(playerId, type);
        GameQueue queue = queues.get(type);

        if (queue != null && queue.enqueue(queuedPlayer)) {
            playerQueue.put(playerId, queuedPlayer);
            return true;
        }
        return false;
    }

    public boolean removeFromQueue(UUID playerId) {
        QueuedPlayer queuedPlayer = playerQueue.remove(playerId);
        if (queuedPlayer == null) {
            return false;
        }

        GameQueue queue = queues.get(queuedPlayer.gameType());
        return queue != null && queue.remove(queuedPlayer);
    }

    public boolean isInQueue(UUID playerId) {
        return playerQueue.containsKey(playerId);
    }

    public GameQueue getQueue(GameType type) {
        return queues.get(type);
    }

    public QueuedPlayer getQueuedPlayer(UUID playerId) {
        return playerQueue.get(playerId);
    }

    public int getQueueSize(GameType type) {
        GameQueue queue = queues.get(type);
        return queue != null ? queue.size() : 0;
    }
}

