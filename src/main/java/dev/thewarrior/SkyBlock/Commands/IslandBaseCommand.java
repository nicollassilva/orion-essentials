package dev.thewarrior.SkyBlock.Commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.thewarrior.Essentials.Utils.StringUtils;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Managers.SkyBlockSettingsManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class IslandBaseCommand extends CommandBase {
    public IslandBaseCommand(final SkyBlockSettingsManager skyBlockSettingsManager, final IslandsManager islandsManager) {
        super("island", "Comando base para gerenciar ilhas no SkyBlock.");

        this.addAliases("is", "ilha");

        this.addSubCommand(new IslandCreateCommand(skyBlockSettingsManager, islandsManager));
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        commandContext.sendMessage(Message.join(
                Message.raw("\n- Como usar o comando ").color(Color.GREEN), Message.raw("/ilha").color(Color.WHITE).bold(true), Message.raw(":\n").color(Color.GREEN),
                Message.raw("Comando para gerenciar sua ilha no SkyBlock.\n\n").color(Color.LIGHT_GRAY).italic(true),
                Message.raw("/ilha ").color(Color.WHITE).bold(true), Message.raw("iniciar").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Cria uma nova ilha\n", 8, " "))
        ));
    }
}
