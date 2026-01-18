package dev.thewarrior.Commands.Warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.WarpManager;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class DelWarpCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> target;
    private final WarpManager warpManager;

    public DelWarpCommand(WarpManager warpManager) {
        super("delwarp", "Remove uma warp existente");

        this.warpManager = warpManager;
        this.target = this.withRequiredArg("nome", "Nome da warp à ser removida", ArgTypes.STRING);

        requirePermission("multicommands.warps.manage");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final String warpName = commandContext.get(this.target);

        this.warpManager.deleteWarp(warpName).thenAccept(removed -> {
            if (!removed) {
                playerRef.sendMessage(Message.raw(
                        String.format(Messages.COMMAND_DELWARP_NOT_FOUND, warpName)
                ).color(Color.RED));
                return;
            }

            playerRef.sendMessage(Message.raw(
                    String.format(Messages.COMMAND_DELWARP_SUCCESS, warpName)
            ).color(Color.GREEN));
        });
    }
}