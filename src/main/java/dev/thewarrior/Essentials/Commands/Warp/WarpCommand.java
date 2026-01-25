package dev.thewarrior.Essentials.Commands.Warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.TeleportManager;
import dev.thewarrior.Essentials.Managers.WarpManager;
import dev.thewarrior.Essentials.Utils.Location;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class WarpCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> target;

    private final WarpManager warpManager;
    private final TeleportManager teleportManager;

    public WarpCommand(final WarpManager warpManager, final TeleportManager teleportManager) {
        super("Teleporta o jogador para a warp especificada.");

        this.warpManager = warpManager;
        this.teleportManager = teleportManager;

        this.target = this.withRequiredArg("nome", "Nome do warp à ser teleportado", ArgTypes.STRING);

        requirePermission(PermissionUtil.getPermission("warps"));
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
        final Location warpLocation = this.warpManager.getWarp(warpName);

        if(warpLocation == null) {
            playerRef.sendMessage(Messages.COMMAND_WARP_NOT_FOUND.color(Color.RED));
            return;
        }

        Vector3d startPosition = playerRef.getTransform().getPosition();

        this.teleportManager.queueTeleport(
                playerRef, ref, store, startPosition,
                warpLocation.getWorld(), warpLocation.getX(), warpLocation.getY(), warpLocation.getZ(), warpLocation.getYaw(), warpLocation.getPitch(),
                Messages.COMMAND_GENERIC_TELEPORT_SUCCESS
        );
    }
}