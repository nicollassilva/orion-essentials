package dev.thewarrior.Commands.Tell;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Data.PlayerCommandData;
import dev.thewarrior.MultiCommands;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class TellOnCommand extends AbstractPlayerCommand {
    public TellOnCommand() {
        super("on", "Habilita receber mensagens privadas de outros jogadores");

        requirePermission("multicommands.tell.on");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final PlayerCommandData playerData = store.getComponent(ref, MultiCommands.PlayerDataComponent);

        if(playerData == null) return;

        if(!playerData.isTellOff()) {
            playerRef.sendMessage(Messages.COMMAND_TELL_ALREADY_ON.color(Color.RED));
            return;
        }

        playerData.setTellOff(false);
        playerRef.sendMessage(Messages.COMMAND_TELL_ON_SUCCESS.color(Color.GREEN));
    }
}