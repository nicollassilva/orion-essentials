package dev.thewarrior.Essentials.Commands.Plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.OrionEssentials;
import dev.thewarrior.Essentials.Utils.PermissionUtil;

import javax.annotation.Nonnull;

public class ReloadCommand extends AbstractPlayerCommand {
    private final OrionEssentials orionEssentials;

    public ReloadCommand(OrionEssentials orionEssentials) {
        super("essentialsreload", "Recarrega as configurações do plugin OrionEssentials");

        this.orionEssentials = orionEssentials;

        requirePermission(PermissionUtil.getPermission("plugin.reload"));
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        this.orionEssentials.reloadConfig(playerRef);
    }
}
