package dev.thewarrior.Commands.Tell;

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
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class TellCommand extends AbstractPlayerCommand {
    private final RequiredArg<PlayerRef> target;
    private final Color color = new Color(214, 120, 206);

    public TellCommand() {
        super("Send a private message to a player");

        this.target = this.withRequiredArg("player", "The player to send a message to", ArgTypes.PLAYER_REF);

        // We don't use this argument directly, but we need it to capture the message
        this.withRequiredArg("message", "The message to send", ArgTypes.STRING);

        requirePermission("multicommands.tell");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        PlayerRef player = this.target.get(commandContext);

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

        final String[] parts = commandContext.getInputString().split(" ");
        final StringBuilder messageBuilder = new StringBuilder();

        for (int i = 2; i < parts.length; i++) {
            messageBuilder.append(parts[i]);

            if (i < parts.length - 1) {
                messageBuilder.append(" ");
            }
        }

        final String message = messageBuilder.toString();

        if(message.isEmpty()) return;

        playerRef.sendMessage(Message.raw("Para [" + player.getUsername() + "]: " + message).color(color));
        player.sendMessage(Message.raw("[" + playerRef.getUsername() + "]: " + message).color(color));
    }
}