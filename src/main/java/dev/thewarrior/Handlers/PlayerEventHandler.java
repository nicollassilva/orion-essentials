package dev.thewarrior.Handlers;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.thewarrior.Commands.Camera.FreeCameraCommand;
import dev.thewarrior.Commands.Tell.TellCommand;
import dev.thewarrior.MultiCommands;
import dev.thewarrior.i18n.Messages;

import java.util.UUID;

public abstract class PlayerEventHandler {
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

    public static void onPlayerDisconnect(PlayerDisconnectEvent event, final MultiCommands plugin) {
        final UUID uuid = event.getPlayerRef().getUuid();

        if(UUIDUtil.isEmptyOrNull(uuid)) return;

        plugin.getTeleportManager().onPlayerQuit(uuid);
        plugin.getRegionEntryProtectionSystem().clearPlayer(uuid);
        plugin.getPermissionManager().invalidatePlayerCache(uuid);

        TellCommand.onPlayerQuit(uuid);
        FreeCameraCommand.onPlayerQuit(uuid);
    }

    public static void onPlayerAddedToWorld(final AddPlayerToWorldEvent event, final MultiCommands plugin) {
        final Holder<EntityStore> holder = event.getHolder();
        final PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());

        if(playerRef == null || !playerRef.isValid()) return;

        final UUID uuid = playerRef.getUuid();

        if(UUIDUtil.isEmptyOrNull(uuid)) return;

        plugin.getRegionManager().invalidatePlayerCache(uuid);
        plugin.getRegionEntryProtectionSystem().clearPlayer(uuid);

        FreeCameraCommand.onPlayerQuit(playerRef.getUuid());
    }
}
