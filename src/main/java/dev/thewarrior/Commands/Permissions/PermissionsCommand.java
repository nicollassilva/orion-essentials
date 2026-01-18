package dev.thewarrior.Commands.Permissions;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.thewarrior.Utils.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class PermissionsCommand extends CommandBase {
    public PermissionsCommand() {
        super("permissions", "Comando para gerenciar as permissões do servidor.");

        addSubCommand(new PermissionsManageCommand());

        requirePermission("multicommands.permissions");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        commandContext.sendMessage(Message.join(
                Message.raw("\n- Como usar o comando ").color(Color.GREEN), Message.raw("/permissions").color(Color.WHITE).bold(true), Message.raw(":\n").color(Color.GREEN),
                Message.raw("Permite que você gerencie as permissões do servidor/jogador.\n\n").color(Color.LIGHT_GRAY).italic(true),
                Message.raw("/permissions ").color(Color.MAGENTA).bold(true), Message.raw("manage").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Gerencia permissões\n", 40, " ")),
                Message.raw("/permissions ").color(Color.MAGENTA).bold(true), Message.raw("set <player> <nome>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Seta a permissão\n", 16, " ")),
                Message.raw("/permissions ").color(Color.MAGENTA).bold(true), Message.raw("remove <player> <nome>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Remove a permissão\n", 8, " "))
        ));
    }
}
