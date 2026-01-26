package dev.thewarrior.MiniGames.Gaming.Event;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class GameEventBus {
    private final Map<Class<?>, Queue<Consumer<?>>> listeners;

    public GameEventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ConcurrentLinkedQueue<>())
                 .offer((Consumer<?>) listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        Queue<Consumer<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null) return;

        for (Consumer<?> listener : eventListeners) {
            try {
                ((Consumer<T>) listener).accept(event);
            } catch (Exception ignored) {}
        }
    }

    public void clear() {
        listeners.clear();
    }
}

