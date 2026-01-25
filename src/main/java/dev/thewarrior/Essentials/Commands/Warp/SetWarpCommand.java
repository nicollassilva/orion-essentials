package dev.thewarrior.Essentials.Commands.Warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.WarpManager;
import dev.thewarrior.Essentials.Utils.Enums.WarpValidationError;
import dev.thewarrior.Essentials.Utils.Location;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class SetWarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;

    private final RequiredArg<String> target;

    public SetWarpCommand(WarpManager warpManager) {
        super("setwarp", "Define uma nova warp");

        this.warpManager = warpManager;
        this.target = this.withRequiredArg("nome", "Nome da warp a ser adicionada", ArgTypes.STRING);

        requirePermission(PermissionUtil.getPermission("warps.manage"));
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

        final TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

        if(transform == null) {
            playerRef.sendMessage(Messages.CANNOT_GET_OWN_PLAYER_POSITION.color(Color.RED));
            return;
        }

        final Vector3d position = transform.getPosition();
        final HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        final Vector3f rotation = headRotation != null ? headRotation.getRotation() : new Vector3f(0, 0, 0);

        this.warpManager.setWarp(warpName, new Location(
                world.getName(),
                position.getX(),
                position.getY(),
                position.getZ(),
                rotation.getY(),
                rotation.getX(),
                rotation.getZ()
        )).thenAccept(error -> {
            if(!error.equals(WarpValidationError.NONE)) {
                playerRef.sendMessage(Message.raw(error.getMessage()).color(Color.RED));
                return;
            }

            playerRef.sendMessage(Message.raw(String.format(
                    Messages.COMMAND_SETWARP_SUCCESS, warpName, world.getName()
            )));
        });
    }
}