package dev.thewarrior.Commands.Tell;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class BaseTellCommand extends CommandBase {
    public BaseTellCommand() {
        super("tell", "Envie uma mensagem privada para um jogador");

        addUsageVariant(new TellCommand());
        addSubCommand(new TellOffCommand());
        addSubCommand(new TellOnCommand());

        requirePermission("multicommands.tell");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        commandContext.sendMessage(Message.join(
                Message.raw("- Como usar o comando ").color(Color.GREEN), Message.raw("/tell").color(Color.WHITE).bold(true), Message.raw(":\n\n").color(Color.GREEN),
                Message.raw("/tell ").color(Color.WHITE).bold(true), Message.raw("<player> <mensagem>").color(Color.YELLOW).bold(true), Message.raw(" Envia uma mensagem privada\n"),
                Message.raw("/telloff").color(Color.WHITE).bold(true), Message.raw(" Desative as mensagens privadas\n"),
                Message.raw("/tellon").color(Color.WHITE).bold(true), Message.raw(" Ative as mensagens privadas\n")
        ));
    }
}
