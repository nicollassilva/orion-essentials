package dev.thewarrior.Handlers;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import dev.thewarrior.Managers.TeleportManager;
import dev.thewarrior.MultiCommands;
import dev.thewarrior.i18n.Messages;

import java.awt.*;
import java.util.UUID;

public class PlayerEventHandler {
    public static void onPlayerReady(final PlayerReadyEvent event) {
        Store<EntityStore> entityStore = event.getPlayerRef().getStore();

        entityStore.ensureComponent(event.getPlayerRef(), MultiCommands.PlayerDataComponent);

        PlayerRef playerRef = entityStore.getComponent(event.getPlayerRef(), PlayerRef.getComponentType());

        if(playerRef != null && playerRef.isValid()) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Messages.PRIMARY_TITLE_ON_LOGIN,
                    Messages.SECOND_TITLE_ON_LOGIN,
                    true
            );
        }
    }

    public static void onPlayerDisconnect(final PlayerDisconnectEvent event, final TeleportManager teleportManager) {
        final UUID playerId = event.getPlayerRef().getUuid();

        teleportManager.onPlayerQuit(playerId);
    }
}
