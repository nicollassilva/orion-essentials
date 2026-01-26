package dev.thewarrior.MiniGames.Gaming.Handler;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;

import java.util.EnumMap;
import java.util.Map;

public class GameHandlerRegistry {
    private final Map<GameType, GameHandler> handlers;

    public GameHandlerRegistry() {
        this.handlers = new EnumMap<>(GameType.class);
    }

    public void register(GameHandler handler) {
        handlers.put(handler.getType(), handler);
    }

    public void unregister(GameType type) {
        handlers.remove(type);
    }

    public GameHandler getHandler(GameType type) {
        return handlers.get(type);
    }

    public boolean hasHandler(GameType type) {
        return handlers.containsKey(type);
    }
}

