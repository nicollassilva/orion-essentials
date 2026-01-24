package dev.thewarrior.Commands.Spawn;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.PluginConfigManager;
import dev.thewarrior.Managers.TeleportManager;
import dev.thewarrior.Utils.Location;
import dev.thewarrior.Utils.PermissionUtil;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class SetSpawnCommand extends AbstractPlayerCommand {
    private final TeleportManager teleportManager;
    private final PluginConfigManager pluginConfigManager;

    public SetSpawnCommand(final PluginConfigManager pluginConfigManager, final TeleportManager teleportManager) {
        super("setspawn", "Define o local de spawn do servidor");

        this.teleportManager = teleportManager;
        this.pluginConfigManager = pluginConfigManager;

        this.addAliases("setlobby");
        this.requirePermission(PermissionUtil.getPermission("spawn.set"));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

        if (transform == null) {
            playerRef.sendMessage(Messages.CANNOT_GET_OWN_PLAYER_POSITION.color(Color.RED));
            return;
        }

        final Vector3d position = transform.getPosition();
        final HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        final Vector3f rotation = (headRotation != null) ? headRotation.getRotation() : new Vector3f(0.0F, 0.0F, 0.0F);

        this.pluginConfigManager.setSpawnLocation(new Location(
                world.getName(),
                position.getX(),
                position.getY(),
                position.getZ(),
                rotation.getY(),
                rotation.getX(),
                rotation.getZ()
        )).thenAccept(_ -> {
            final Vector3d spawnPosition = new Vector3d(position.getX(), position.getY(), position.getZ());
            final Vector3f spawnRotation = new Vector3f(0, rotation.getY(), 0);
            final Transform spawnTransform = new Transform(spawnPosition, spawnRotation);

            world.getWorldConfig().setSpawnProvider(new GlobalSpawnProvider(spawnTransform));

            playerRef.sendMessage(Messages.COMMAND_SET_SPAWN_SUCCESS.color(Color.GREEN));
        });
    }
}