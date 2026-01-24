package dev.thewarrior.Commands.Permissions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.PermissionManager;
import dev.thewarrior.Pages.Permissions.PermissionsPage;
import dev.thewarrior.Utils.PermissionUtil;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PermissionsManageCommand extends AbstractPlayerCommand {
    private final PermissionManager permissionManager;

    public PermissionsManageCommand(PermissionManager permissionManager) {
        super("manage", "Gerencie as permissões do servidor.");

        this.permissionManager = permissionManager;

        requirePermission(PermissionUtil.getPermission("permissions.manage"));
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

        final PermissionsPage page = new PermissionsPage(playerRef, this.permissionManager);

        player.getPageManager().openCustomPage(ref, store, page);
    }
}
