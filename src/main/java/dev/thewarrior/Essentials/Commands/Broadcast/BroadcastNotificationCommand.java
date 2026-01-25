package dev.thewarrior.Essentials.Commands.Broadcast;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class BroadcastNotificationCommand extends AbstractPlayerCommand {
    public BroadcastNotificationCommand() {
        super("notif", "Envia uma notificação para todos os jogadores online");

        requirePermission(PermissionUtil.getPermission("broadcast.notification"));
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
        String[] parts = rawInput.split("\\s+", 3);

        if(parts.length <3) return;

        NotificationUtil.sendNotificationToUniverse(
                ColorUtil.colorize(parts[2]),
                Message.raw("Mensagem do Servidor"),
                NotificationStyle.Default
        );
    }
}