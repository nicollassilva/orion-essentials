package dev.thewarrior.Commands.Tpa;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.Data.Teleport.TpaRequest;
import dev.thewarrior.Managers.TeleportManager;
import dev.thewarrior.Managers.TpaManager;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.awt.*;

public class TpacceptCommand extends AbstractPlayerCommand {
    private final TpaManager tpaManager;
    private final TeleportManager teleportManager;

    public TpacceptCommand(@Nonnull TpaManager tpaManager, @Nonnull TeleportManager teleportManager) {
        super("tpaccept", "Aceita o pedido de teletransporte de outro jogador.");

        this.tpaManager = tpaManager;
        this.teleportManager = teleportManager;

        requirePermission("multicommands.tpaccept");
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
        String[] parts = input.split("\\s+", 2); // ["tpaccept", "<player>"]
        String targetName = parts.length > 1 ? parts[1] : null;

        final TpaRequest request = this.tpaManager.acceptRequest(playerRef, targetName);

        if(request == null) {
            playerRef.sendMessage(Messages.COMMAND_TPA_NO_PENDING_REQUESTS.color(Color.YELLOW));
            return;
        }

        final PlayerRef requester = Universe.get().getPlayer(request.getRequesterUuid());

        if(requester == null) {
            playerRef.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        final Ref<EntityStore> requesterRef = requester.getReference();

        if(requesterRef == null || !requesterRef.isValid()) {
            playerRef.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        final Store<EntityStore> requesterStore = requesterRef.getStore();

        playerRef.sendMessage(Message.raw(
                String.format(Messages.COMMAND_TPA_REQUEST_ACCEPTED, requester.getUsername())
        ).color(Color.GREEN));

        Vector3d startPosition = requester.getTransform().getPosition();

        teleportManager.queueTeleportToPlayer(
                requester, requesterRef, requesterStore, startPosition,
                playerRef,  // target player
                Message.raw(
                        String.format(Messages.COMMAND_TPA_REQUEST_TARGET_ACCEPTED, playerRef.getUsername())
                ).color(Color.GREEN)
        );
    }
}

