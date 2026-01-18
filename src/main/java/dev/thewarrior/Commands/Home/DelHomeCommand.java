package dev.thewarrior.Commands.Home;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
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

public class DelHomeCommand extends AbstractPlayerCommand {
    public DelHomeCommand() {
        super("delhome", "Exclui uma home previamente definida");

        requirePermission("multicommands.delhome");
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
        final PlayerCommandComponent component = store.getComponent(ref, MultiCommands.PlayerDataComponent);

        if(component == null) {
            playerRef.sendMessage(Messages.CANNOT_GET_OWN_PLAYER_DATA.color(Color.RED));
            return;
        }

        final String rawInput = commandContext.getInputString();
        final String[] parts = rawInput.split("\\s+", 2); // [command, name]
        final String name = parts.length > 1 ? parts[1].trim() : "home";

        if(!component.hasHome(name)) {
            playerRef.sendMessage(Message.raw(String.format(Messages.COMMAND_HOME_NOT_EXISTS, name)).color(Color.YELLOW));
            return;
        }

        component.removeHome(name);

        playerRef.sendMessage(Message.raw(String.format(Messages.COMMAND_DEL_HOME_SUCCESS, name)).color(Color.GREEN));
    }
}
