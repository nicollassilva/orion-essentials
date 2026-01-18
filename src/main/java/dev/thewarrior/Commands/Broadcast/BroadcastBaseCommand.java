package dev.thewarrior.Commands.Broadcast;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.thewarrior.Managers.PluginConfigManager;
import dev.thewarrior.Utils.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class BroadcastBaseCommand extends CommandBase {
    public BroadcastBaseCommand(final PluginConfigManager pluginConfigManager) {
        super("broadcast", "Conjunto de comandos para enviar mensagens para todos os jogadores online");

        addSubCommand(new BroadcastChatCommand(pluginConfigManager));
        addSubCommand(new BroadcastTitleCommand());

        requirePermission("multicommands.broadcast");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        commandContext.sendMessage(Message.join(
                Message.raw("\n- Como usar o comando ").color(Color.GREEN), Message.raw("/broadcast").color(Color.WHITE).bold(true), Message.raw(":\n").color(Color.GREEN),
                Message.raw("Permite que você envie mensagens para todos os jogadores online.\n\n").color(Color.LIGHT_GRAY).italic(true),
                Message.raw("/broadcast ").color(Color.WHITE).bold(true), Message.raw("chat <mensagem>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Envia a mensagem no chat \n", 8, " ")),
                Message.raw("/broadcast ").color(Color.WHITE).bold(true), Message.raw("title <mensagem>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Envia a mensagem como título \n", 8, " "))
        ));
    }
}
