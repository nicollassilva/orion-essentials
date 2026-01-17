package dev.thewarrior.Handlers;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.MultiCommands;

public class PlayerEventHandler {
    public static void onPlayerReady(final PlayerReadyEvent event) {
        Store<EntityStore> entityStore = event.getPlayerRef().getStore();

        entityStore.ensureComponent(event.getPlayerRef(), MultiCommands.PlayerDataComponent);
    }
}
