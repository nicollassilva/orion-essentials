package dev.thewarrior.Commands.Camera;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.camera.SetFlyCameraMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.i18n.Messages;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Set;
import java.util.UUID;

public class FreeCameraCommand extends AbstractPlayerCommand {
    private static final Set<UUID> players = new ObjectOpenHashSet<>();

    public FreeCameraCommand() {
        super("freecam", "Habilita/desabilita o modo de câmera livre.");

        requirePermission("multicommands.freecam");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        final UUID uuid = playerRef.getUuid();
        boolean isEnabling = !players.contains(uuid);

        playerRef.getPacketHandler().write(new SetFlyCameraMode(isEnabling));

        if (isEnabling) {
            players.add(uuid);
            playerRef.sendMessage(Messages.COMMAND_FREE_CAMERA_ENABLED.color(Color.CYAN));
        } else {
            players.remove(uuid);
            playerRef.sendMessage(Messages.COMMAND_FREE_CAMERA_DISABLED.color(Color.CYAN));
        }
    }

    public static void onPlayerQuit(UUID uuid) {
        players.remove(uuid);
    }
}
