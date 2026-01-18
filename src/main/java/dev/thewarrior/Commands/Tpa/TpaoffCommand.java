package dev.thewarrior.Commands.Tpa;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Components.PlayerCommandComponent;
import dev.thewarrior.MultiCommands;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class TpaoffCommand extends AbstractPlayerCommand {
    public TpaoffCommand() {
        super("tpaoff", "Desativa os pedidos de teleporte enviados por outros jogadores");

        requirePermission("multicommands.tpaoff");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        PlayerCommandComponent playerData = store.getComponent(ref, MultiCommands.PlayerDataComponent);

        if(playerData == null) return;

        if(playerData.isTpaOff()) {
            playerRef.sendMessage(Messages.COMMAND_TPA_ALREADY_OFF.color(Color.YELLOW));
            return;
        }

        playerData.setTpaOff(true);
        playerRef.sendMessage(Messages.COMMAND_TPA_OFF_SUCCESS.color(Color.GREEN));
    }
}

