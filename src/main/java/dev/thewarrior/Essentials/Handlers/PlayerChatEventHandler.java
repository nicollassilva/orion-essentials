package dev.thewarrior.Essentials.Handlers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent.Formatter;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Essentials.Managers.Data.Region.Flag.RegionFlag;
import dev.thewarrior.Essentials.Managers.PermissionManager;
import dev.thewarrior.Essentials.Managers.RegionManager;
import dev.thewarrior.OrionEssentials;
import dev.thewarrior.Essentials.Utils.ColorUtil;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PlayerChatEventHandler {
    public static String commandBlockFlagName = RegionFlag.COMMANDS.getName();
    public static DefaultFormatter defaultFormatter;

    public static void onEvent(final PlayerChatEvent event, final OrionEssentials plugin) {
        if(defaultFormatter == null) {
            defaultFormatter = new DefaultFormatter(plugin.getPermissionManager());
        }

        event.setFormatter(defaultFormatter);

        if(!event.getContent().startsWith("/")) return;

        treatPossibleCommand(event, plugin.getRegionManager());
    }

    protected static void treatPossibleCommand(final PlayerChatEvent event, final RegionManager regionManager) {
        final PlayerRef sender = event.getSender();

        if(!sender.isValid()) return;

        final Ref<EntityStore> ref = sender.getReference();

        if(ref == null || !ref.isValid()) return;

        final Store<EntityStore> store = ref.getStore();
        final TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        final UUID worldId = sender.getWorldUuid();

        if(transformComponent == null || worldId == null) return;

        final World world = Universe.get().getWorld(sender.getWorldUuid());

        if(world == null) return;

        final RegionData currentRegion = regionManager.getHighestPriorityRegion(sender.getUuid(), transformComponent, world);

        if(currentRegion == null) return;
        if(!currentRegion.getFlags().hasFlag(commandBlockFlagName)) return;

        String command = event.getContent().split("\\s+")[0].substring(1).toLowerCase();

        if(!currentRegion.getFlags().checkMappedPermission(commandBlockFlagName, command)) return;

        sender.sendMessage(ColorUtil.colorize("&cVocê não tem permissão para usar esse comando nessa área."));

        event.setCancelled(true);
    }

    public static class DefaultFormatter implements Formatter {
        private final PermissionManager permissionManager;

        public DefaultFormatter(final PermissionManager permissionManager) {
            this.permissionManager = permissionManager;
        }

        @Nonnull
        public Message format(@Nonnull PlayerRef playerRef, @Nonnull String content) {
            return this.permissionManager.formatChatMessage(playerRef, content);
        }
    }
}
