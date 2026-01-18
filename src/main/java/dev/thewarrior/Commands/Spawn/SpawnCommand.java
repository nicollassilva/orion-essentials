package dev.thewarrior.Commands.Spawn;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.PluginConfigManager;
import dev.thewarrior.Managers.TeleportManager;
import dev.thewarrior.Utils.Location;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class SpawnCommand extends AbstractPlayerCommand {
    private final TeleportManager teleportManager;
    private final PluginConfigManager pluginConfigManager;

    public SpawnCommand(final PluginConfigManager pluginConfigManager, final TeleportManager teleportManager) {
        super("spawn", "Teleporta o jogador para o spawn do servidor");

        this.teleportManager = teleportManager;
        this.pluginConfigManager = pluginConfigManager;

        this.addAliases("lobby", "s");
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final Location spawnLocation = this.pluginConfigManager.getSpawnLocation();

        if(spawnLocation == null) {
            playerRef.sendMessage(Messages.COMMAND_SPAWN_NOT_SET.color(Color.YELLOW));
            return;
        }

        Vector3d startPosition = playerRef.getTransform().getPosition();

        this.teleportManager.queueTeleport(
                playerRef,
                ref,
                store,
                startPosition,
                spawnLocation.getWorld(),
                spawnLocation.getX(),
                spawnLocation.getY(),
                spawnLocation.getZ(),
                spawnLocation.getYaw(),
                spawnLocation.getPitch(),
                Messages.COMMAND_SPAWN_SUCCESS
        );
    }
}