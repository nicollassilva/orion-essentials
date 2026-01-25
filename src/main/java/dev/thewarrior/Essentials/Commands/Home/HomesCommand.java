package dev.thewarrior.Essentials.Commands.Home;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Components.PlayerCommandComponent;
import dev.thewarrior.OrionBootstrap;
import dev.thewarrior.Essentials.Utils.NamedLocation;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class HomesCommand extends AbstractPlayerCommand {
    public HomesCommand() {
        super("homes", "Lista todas as suas homes definidas");

        requirePermission(PermissionUtil.getPermission("homes.list"));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final PlayerCommandComponent component = store.getComponent(ref, OrionBootstrap.PlayerDataComponent);

        if(component == null) {
            playerRef.sendMessage(Messages.CANNOT_GET_OWN_PLAYER_DATA.color(Color.RED));
            return;
        }

        final StringBuilder homes = new StringBuilder();

        for (NamedLocation location : component.getHomes()) {
            homes.append(location.getName()).append("\n");
        }

        playerRef.sendMessage(Message.join(
                Messages.COMMAND_HOME_TITLE.color(Color.WHITE),
                Message.raw(homes.toString()).color(Color.ORANGE)
        ));
    }
}