package dev.thewarrior.SkyBlock.Handlers;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandLastVisitData;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.SkyBlockBootstrap;

import java.util.UUID;

public class SkyBlockPlayerEventHandler {
    public static String visitBypassPermission = PermissionUtil.getPermission("skyblock.visit.bypass");

    public static void onPlayerAddedToWorld(final AddPlayerToWorldEvent event, final SkyBlockBootstrap plugin) {
        if(!event.getWorld().getName().startsWith("island_")) return;

        event.setBroadcastJoinMessage(false);

        final Holder<EntityStore> holder = event.getHolder();
        final PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());

        if(playerRef == null || !playerRef.isValid()) return;

        final UUID uuid = playerRef.getUuid();

        if(UUIDUtil.isEmptyOrNull(uuid)) return;

        final IslandData islandData = plugin.getIslandManager().getIslandByWorldName(event.getWorld().getName());

        if(islandData == null) {
            Logger.error("IslandData não encontrado para o jogador " + playerRef.getUsername() + " ao entrar no mundo " + event.getWorld().getName());
            return;
        }

        //if(!islandData.getOwnerId().equals(uuid)) {
            final Player player = holder.getComponent(Player.getComponentType());

            if(player != null && !player.hasPermission(visitBypassPermission)) {
                islandData.addLastVisitData(new IslandLastVisitData(playerRef.getUsername()));

                plugin.getIslandManager().save(islandData);
            }
        //}

        if(islandData.getEnterTitle() != null && !islandData.getEnterTitle().isBlank()) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw(islandData.getEnterTitle()),
                    Message.raw("Explore e aproveite sua estadia!"),
                    true
            );
        }
    }
}
