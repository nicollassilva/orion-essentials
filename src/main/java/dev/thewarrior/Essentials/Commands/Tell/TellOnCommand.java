package dev.thewarrior.Essentials.Commands.Tell;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Components.PlayerCommandComponent;
import dev.thewarrior.OrionBootstrap;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class TellOnCommand extends AbstractPlayerCommand {
    public TellOnCommand() {
        super("tellon", "Habilita receber mensagens privadas de outros jogadores");

        requirePermission(PermissionUtil.getPermission("tells.on"));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final PlayerCommandComponent playerData = store.getComponent(ref, OrionBootstrap.PlayerDataComponent);

        if(playerData == null) return;

        if(!playerData.isTellOff()) {
            playerRef.sendMessage(Messages.COMMAND_TELL_ALREADY_ON.color(Color.YELLOW));
            return;
        }

        playerData.setTellOff(false);
        playerRef.sendMessage(Messages.COMMAND_TELL_ON_SUCCESS.color(Color.GREEN));
    }
}