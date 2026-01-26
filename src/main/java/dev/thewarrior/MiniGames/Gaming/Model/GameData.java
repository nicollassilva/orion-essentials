package dev.thewarrior.MiniGames.Gaming.Model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameData {
    private final Map<String, Object> data;

    public GameData() {
        this.data = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> T get(String key, T defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        return (T) value;
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public void clear() {
        data.clear();
    }
}

