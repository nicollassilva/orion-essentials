package dev.thewarrior.Commands.MultiCommands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.MultiCommands;

import javax.annotation.Nonnull;

public class PluginReloadCommand extends AbstractPlayerCommand {
    private final MultiCommands multiCommands;

    public PluginReloadCommand(MultiCommands multiCommands) {
        super("mcreload", "Recarrega as configurações do plugin MultiCommands");

        this.multiCommands = multiCommands;

        requirePermission("multicommands.reload");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        this.multiCommands.reloadConfig(playerRef);
    }
}
