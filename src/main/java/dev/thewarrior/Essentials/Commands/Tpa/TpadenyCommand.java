package dev.thewarrior.Essentials.Commands.Tpa;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.Data.Teleport.TpaRequest;
import dev.thewarrior.Essentials.Managers.TpaManager;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.awt.*;

public class TpadenyCommand extends AbstractPlayerCommand {
    private final TpaManager tpaManager;

    public TpadenyCommand(@Nonnull TpaManager tpaManager) {
        super("tpadeny", "Recusa o pedido de teleporte de outro jogador.");

        this.tpaManager = tpaManager;

        requirePermission(PermissionUtil.getPermission("tpas.deny"));
        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext context,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        String input = context.getInputString();
        String[] parts = input.split("\\s+", 2);
        String targetName = parts.length > 1 ? parts[1] : null;

        final TpaRequest request = this.tpaManager.invalidateRequest(playerRef, targetName);

        if(request == null) {
            playerRef.sendMessage(Messages.COMMAND_TPA_NO_PENDING_REQUESTS.color(Color.YELLOW));
            return;
        }

        final PlayerRef requester = Universe.get().getPlayer(request.getRequesterUuid());

        if(requester != null) {
            requester.sendMessage(Message.raw(
                    String.format(Messages.COMMAND_TPA_REQUEST_TARGET_DENIED, playerRef.getUsername())
            ).color(Color.PINK));
        }

        playerRef.sendMessage(Message.raw(
                String.format(Messages.COMMAND_TPA_REQUEST_DENIED, request.getRequesterName())
        ).color(Color.YELLOW));
    }
}

