package dev.thewarrior.Commands.Permissions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Pages.Permissions.PermissionsPage;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PermissionsManageCommand extends AbstractPlayerCommand {
    public PermissionsManageCommand() {
        super("manage", "Gerencie as permissões do servidor.");

        requirePermission("multicommands.permissions.manage");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if(player == null) {
            commandContext.sendMessage(Messages.PLAYER_NOT_FOUND);
            return;
        }

        final PermissionsPage page = new PermissionsPage(playerRef, PermissionsModule.get().getProviders());

        player.getPageManager().openCustomPage(ref, store, page);
    }
}
