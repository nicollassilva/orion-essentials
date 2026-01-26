package dev.thewarrior.MiniGames.Gaming.Arena;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class ArenaManager {
    private final Map<GameType, ArenaPool> pools;

    public ArenaManager() {
        this.pools = new EnumMap<>(GameType.class);
        for (GameType type : GameType.values()) {
            pools.put(type, new ArenaPool(type));
        }
    }

    public Game acquireArena(GameType type) {
        ArenaPool pool = pools.get(type);
        return pool != null ? pool.acquire() : null;
    }

    public void releaseArena(Game arena) {
        ArenaPool pool = pools.get(arena.getType());
        if (pool != null) {
            pool.release(arena);
        }
    }

    public Game findGame(UUID gameId) {
        for (ArenaPool pool : pools.values()) {
            Game game = pool.getActive(gameId);
            if (game != null) {
                return game;
            }
        }
        return null;
    }

    public Game findAvailableGame(GameType type) {
        ArenaPool pool = pools.get(type);
        if (pool == null) return null;

        for (Game game : pool.getActiveArenas().values()) {
            if (game.getState().canJoin() && !game.isFull()) {
                return game;
            }
        }
        return null;
    }

    public ArenaPool getPool(GameType type) {
        return pools.get(type);
    }
}

