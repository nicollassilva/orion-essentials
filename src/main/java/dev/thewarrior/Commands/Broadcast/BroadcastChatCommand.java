package dev.thewarrior.Commands.Broadcast;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.PluginConfigManager;
import dev.thewarrior.Utils.ColorUtil;
import dev.thewarrior.Utils.PermissionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class BroadcastChatCommand extends AbstractPlayerCommand {
    private final PluginConfigManager pluginConfigManager;

    public BroadcastChatCommand(final PluginConfigManager pluginConfigManager) {
        super("chat", "Envia uma mensagem para todos os jogadores online");

        this.pluginConfigManager = pluginConfigManager;

        requirePermission(PermissionUtil.getPermission("broadcast.chat"));
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

        Universe.get().sendMessage(ColorUtil.colorize(
                this.pluginConfigManager.getBroadcastFormat().replace("{message}", parts[2])
        ));
    }
}