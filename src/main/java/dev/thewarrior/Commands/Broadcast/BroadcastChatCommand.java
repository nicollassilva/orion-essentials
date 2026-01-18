package dev.thewarrior.Commands.Broadcast;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.PluginConfigManager;
import dev.thewarrior.Utils.ColorUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class BroadcastChatCommand extends AbstractPlayerCommand {
    private final PluginConfigManager pluginConfigManager;

    public BroadcastChatCommand(final PluginConfigManager pluginConfigManager) {
        super("chat", "Envia uma mensagem para todos os jogadores online");

        this.pluginConfigManager = pluginConfigManager;

        // We don't use this argument directly, but we need it to capture the message
        this.withRequiredArg("mensagem", "Mensagem à ser enviada", ArgTypes.STRING);

        requirePermission("multicommands.broadcast.message");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final String[] parts = commandContext.getInputString().split(" ");
        final StringBuilder messageBuilder = new StringBuilder();

        for (int i = 2; i < parts.length; i++) {
            messageBuilder.append(parts[i]);

            if (i < parts.length - 1) {
                messageBuilder.append(" ");
            }
        }

        final String text = messageBuilder.toString();

        if(text.isEmpty()) return;

        Universe.get().sendMessage(ColorUtil.colorize(
                this.pluginConfigManager.getBroadcastFormat().replace("{message}", text)
        ));
    }
}