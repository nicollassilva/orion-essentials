package dev.thewarrior.Commands.Warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.WarpManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WarpsCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;

    public WarpsCommand(WarpManager warpManager) {
        super("warps", "Teleporta o jogador para a warp especificada.");

        this.warpManager = warpManager;

        requirePermission("multicommands.warp");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        // TODO: Implementar listagem de warps (com UI)
    }
}