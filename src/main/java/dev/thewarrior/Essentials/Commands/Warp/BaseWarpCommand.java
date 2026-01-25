package dev.thewarrior.Essentials.Commands.Warp;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.thewarrior.Essentials.Managers.TeleportManager;
import dev.thewarrior.Essentials.Managers.WarpManager;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.Utils.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class BaseWarpCommand extends CommandBase {
    public final String managePermission = PermissionUtil.getPermission("warps.manage");

    public BaseWarpCommand(final WarpManager warpManager, final TeleportManager teleportManager) {
        super("warp", "Conjunto de comandos para teleportar à warps.");

        addUsageVariant(new WarpCommand(warpManager, teleportManager));

        requirePermission(PermissionUtil.getPermission("warps"));
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        Message helper = Message.join(
                Message.raw("\n- Como usar o comando ").color(Color.GREEN), Message.raw("/warp").color(Color.WHITE).bold(true), Message.raw(":\n").color(Color.GREEN),
                Message.raw("Permite que você se teleporte para áreas do servidor.\n\n").color(Color.LIGHT_GRAY).italic(true),
                Message.raw("/warp ").color(Color.WHITE).bold(true), Message.raw("<nome>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Teleporte para a warp especificada\n", 13, " ")),
                Message.raw("/warps").color(Color.WHITE).bold(true), Message.raw(StringUtils.padLeft("Veja a lista de warps disponíveis\n", 27, " "))
        );

        if(commandContext.sender().hasPermission(this.managePermission)) {
            helper.insertAll(
                    Message.raw("/setwarp ").color(Color.MAGENTA).bold(true), Message.raw("<nome>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Define uma nova warp\n", 8, " ")),
                    Message.raw("/delwarp ").color(Color.MAGENTA).bold(true), Message.raw("<nome>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Deleta a warp especificada\n", 8, " "))
            );
        }

        commandContext.sendMessage(helper);
    }
}
