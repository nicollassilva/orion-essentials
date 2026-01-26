package dev.thewarrior.MiniGames.Gaming.Model;

import dev.thewarrior.MiniGames.Gaming.Enums.GameState;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Player.GamePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Game {
    private final UUID id;
    private final GameType type;
    private final AtomicReference<GameState> state;
    private final Map<UUID, GamePlayer> players;
    private final AtomicLong lastVisitTimestamp;

    public Game(GameType type) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.state = new AtomicReference<>(GameState.WAITING);
        this.players = new ConcurrentHashMap<>();
        this.lastVisitTimestamp = new AtomicLong(System.currentTimeMillis());
    }

    public UUID getId() {
        return id;
    }
}
