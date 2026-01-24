package dev.thewarrior.Commands.Teleports;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Utils.PermissionUtil;
import dev.thewarrior.Utils.TeleportUtil;
import dev.thewarrior.i18n.Messages;

import javax.annotation.Nonnull;
import java.awt.*;

public class TpHereCommand extends AbstractPlayerCommand {
    private final RequiredArg<PlayerRef> targetArg;

    public TpHereCommand() {
        super("tphere", "Teleporta um jogador até você.");

        this.targetArg = withRequiredArg("player", "Player à ser teleportado", ArgTypes.PLAYER_REF);

        requirePermission(PermissionUtil.getPermission("tphere"));
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        PlayerRef target = context.get(targetArg);

        if (target == null) {
            playerRef.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        if (target.getUuid().equals(playerRef.getUuid())) {
            playerRef.sendMessage(Messages.CANNOT_TELEPORT_YOURSELF.color(Color.YELLOW));
            return;
        }

        TeleportUtil.teleportToPlayer(target, playerRef);

        context.sendMessage(Message.raw(String.format(
                Messages.COMMAND_TP_HERE_SUCCESS, target.getUsername()
        )).color(Color.GREEN));

        target.sendMessage(Message.raw(String.format(
                Messages.COMMAND_TP_HERE_TARGET_SUCCESS, playerRef.getUsername()
        )).color(Color.GREEN));
    }
}