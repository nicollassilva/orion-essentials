package dev.thewarrior.Essentials.Handlers;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.thewarrior.Essentials.Commands.Camera.FreeCameraCommand;
import dev.thewarrior.Essentials.Commands.Tell.TellCommand;
import dev.thewarrior.Essentials.Managers.Data.Config.PlayerEntryConfig;
import dev.thewarrior.Essentials.Utils.*;
import dev.thewarrior.OrionBootstrap;

import java.util.UUID;

public abstract class EssentialsPlayerEventHandler {
    public static void onPlayerConnect(final PlayerConnectEvent event, final OrionBootstrap plugin) {
        final PlayerRef playerRef = event.getPlayerRef();
        final PlayerEntryConfig entryConfig = plugin.getConfig().getData().getPlayerJoinConfig();

        boolean hasJoined = plugin.getPlayerHistoryManager().hasHistory(playerRef.getUuid());
        boolean isEveryJoinToSpawn = entryConfig.isTeleportToSpawnOnJoinEnabled();

        if(!isEveryJoinToSpawn && hasJoined) return;

        Location spawn = plugin.getConfig().getSpawnLocation();

        if(spawn == null) return;

        World world = Universe.get().getWorld(spawn.getWorld());

        if(world == null) {
            Logger.error("O mundo de spawn não foi encontrado ao tentar teleportar o jogador " + playerRef.getUsername() + " para o spawn.");
            return;
        }

        event.setWorld(world);

        final Holder<EntityStore> holder = event.getHolder();
        final Vector3f bodyRotation = new Vector3f(0, TeleportUtil.roundToCardinalYaw(spawn.getYaw()), 0);

        holder.putComponent(TransformComponent.getComponentType(), new TransformComponent(spawn.getLocationVector(), bodyRotation));
        holder.ensureAndGetComponent(HeadRotation.getComponentType()).teleportRotation(bodyRotation);

        if(entryConfig.isEveryJoinTitleEnabled()) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw(entryConfig.getEveryJoinTitleTitle().replace("{player}", playerRef.getUsername())),
                    Message.raw(entryConfig.getEveryJoinTitleSubtitle().replace("{player}", playerRef.getUsername())),
                    true
            );
        }

        if (!hasJoined) {
            onFirstJoin(playerRef, plugin, entryConfig);
        } else {
            onReturnJoin(playerRef, plugin, entryConfig);
        }
    }

    public static void onPlayerReady(final PlayerReadyEvent event, final OrionBootstrap plugin) {
        Store<EntityStore> entityStore = event.getPlayerRef().getStore();

        entityStore.ensureComponent(event.getPlayerRef(), OrionBootstrap.PlayerDataComponent);
    }

    private static void onFirstJoin(final PlayerRef playerRef, final OrionBootstrap plugin, final PlayerEntryConfig entryConfig) {
        plugin.getPlayerHistoryManager().addHistory(playerRef.getUuid());

        if(entryConfig.isFirstJoinMessageEnabled()) {
            playerRef.sendMessage(ColorUtil.colorize(
                    entryConfig.getFirstJoinMessage().replace("{player}", playerRef.getUsername())
            ));
        }

        String firstSound = entryConfig.getFirstJoinSound();

        if(firstSound != null && !firstSound.isEmpty()) {
            SoundsUtil.playSound(playerRef, firstSound);
        }
    }

    private static void onReturnJoin(final PlayerRef playerRef, final OrionBootstrap plugin, final PlayerEntryConfig entryConfig) {
        if(entryConfig.isReturnJoinMessageEnabled()) {
            playerRef.sendMessage(ColorUtil.colorize(
                    entryConfig.getReturnJoinMessage().replace("{player}", playerRef.getUsername())
            ));
        }
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event, final OrionBootstrap plugin) {
        final UUID uuid = event.getPlayerRef().getUuid();

        if(UUIDUtil.isEmptyOrNull(uuid)) return;

        plugin.getTeleportManager().onPlayerQuit(uuid);
        plugin.getRegionEntryProtectionSystem().clearPlayer(uuid);
        plugin.getPermissionManager().invalidatePlayerCache(uuid);

        TellCommand.onPlayerQuit(uuid);
        FreeCameraCommand.onPlayerQuit(uuid);
    }

    public static void onPlayerAddedToWorld(final AddPlayerToWorldEvent event, final OrionBootstrap plugin) {
        event.setBroadcastJoinMessage(false);

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
