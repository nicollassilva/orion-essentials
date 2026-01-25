package dev.thewarrior.Essentials.Commands.Tpa;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Components.PlayerCommandComponent;
import dev.thewarrior.OrionEssentials;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class TpaonCommand extends AbstractPlayerCommand {
    public TpaonCommand() {
        super("tpaon", "Habilita receber pedidos de teleporte de outros jogadores");

        requirePermission(PermissionUtil.getPermission("tpas.on"));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final PlayerCommandComponent playerData = store.getComponent(ref, OrionEssentials.PlayerDataComponent);

        if(playerData == null) return;

        if(!playerData.isTpaOff()) {
            playerRef.sendMessage(Messages.COMMAND_TPA_ALREADY_ON.color(Color.YELLOW));
            return;
        }

        playerData.setTpaOff(false);
        playerRef.sendMessage(Messages.COMMAND_TPA_ON_SUCCESS.color(Color.GREEN));
    }
}

