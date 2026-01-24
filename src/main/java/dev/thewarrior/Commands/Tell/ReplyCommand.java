package dev.thewarrior.Commands.Tell;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Utils.PermissionUtil;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;
import java.util.UUID;

public class ReplyCommand extends AbstractPlayerCommand {
    private final Color color = new Color(214, 120, 206);

    public ReplyCommand() {
        super("reply", "Responde à última mensagem privada recebida");

        addAliases("r");
        requirePermission(PermissionUtil.getPermission("tells.reply"));
        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        String rawInput = commandContext.getInputString();
        String[] parts = rawInput.split("\\s+", 2);

        if (parts.length < 2) return;

        final String message = parts[1];
        final UUID lastPartnerId = TellCommand.getLastMessagePartner(playerRef.getUuid());

        if(lastPartnerId == null) return;

        final PlayerRef player = Universe.get().getPlayer(lastPartnerId);

        if(player == null || !player.isValid()) {
            commandContext.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        if(playerRef.getUsername().equals(player.getUsername())) {
            commandContext.sendMessage(Messages.CANNOT_TELL_YOURSELF.color(Color.YELLOW));
            return;
        }

        final Ref<EntityStore> playerEntity = player.getReference();

        if(playerEntity == null || !playerEntity.isValid()) {
            commandContext.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        if(message.isEmpty()) return;

        playerRef.sendMessage(Message.raw("Para [" + player.getUsername() + "]: " + message).color(color));
        player.sendMessage(Message.raw("[" + playerRef.getUsername() + "]: " + message).color(color));
    }
}