package dev.thewarrior.Commands.Warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.WarpManager;
import dev.thewarrior.Utils.PermissionUtil;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class WarpsCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;

    public WarpsCommand(WarpManager warpManager) {
        super("warps", "Teleporta o jogador para a warp especificada.");

        this.warpManager = warpManager;

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

        if (this.warpManager.getWarps().isEmpty()) {
            playerRef.sendMessage(Messages.COMMAND_WARPS_EMPTY.color(Color.WHITE));
            return;
        }

        final StringBuilder warpList = new StringBuilder();

        for (String warpName : this.warpManager.getWarps().keySet()) {
            warpList.append(warpName).append("\n");
        }

        playerRef.sendMessage(Message.join(
                Messages.COMMAND_WARPS_TITLE.color(Color.WHITE),
                Message.raw(warpList.toString()).color(Color.ORANGE)
        ));
    }
}