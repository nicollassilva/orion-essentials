package dev.thewarrior.Essentials.Commands.Region;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Essentials.Managers.RegionManager;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.Utils.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class RegionBaseCommand extends CommandBase {
    public RegionBaseCommand(RegionManager regionManager) {
        super("region", "Permite gerenciar regiões protegidas no servidor.");

        addSubCommand(new RegionCreateCommand(regionManager));
        addSubCommand(new RegionListCommand(regionManager));
        addSubCommand(new RegionDetailCommand(regionManager));
        addSubCommand(new RegionDeleteCommand(regionManager));
        addSubCommand(new RegionUpdateCommand(regionManager));
        addSubCommand(new RegionDeselectCommand());
        addSubCommand(new RegionFlagCommand(regionManager));

        requirePermission(PermissionUtil.getPermission("regions"));
    }

    public boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext commandContext) {
        commandContext.sendMessage(Message.join(
                Message.raw("\n- Como usar o comando ").color(Color.GREEN), Message.raw("/region").color(Color.WHITE).bold(true), Message.raw(":\n").color(Color.GREEN),
                Message.raw("Permite gerenciar regiões protegidas no servidor.\n\n").color(Color.LIGHT_GRAY).italic(true),
                Message.raw("/region create").color(Color.WHITE).bold(true), Message.raw(" <type> <name>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Cria uma nova região\n", 10, " ")),
                Message.raw("/region list").color(Color.WHITE).bold(true), Message.raw(StringUtils.padLeft("Mostra todas as regiões criadas\n", 45, " ")),
                Message.raw("/region detail").color(Color.WHITE).bold(true), Message.raw(" <name> [show]").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Detalhes da região\n", 11, " ")),
                Message.raw("/region deselect").color(Color.WHITE).bold(true), Message.raw(StringUtils.padLeft("Remove a seleção atual\n", 35, " ")),
                Message.raw("/region delete").color(Color.WHITE).bold(true), Message.raw(" <name>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Remove uma região\n", 28, " ")),
                Message.raw("/region update").color(Color.WHITE).bold(true), Message.raw(" <name> [options]").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Atualiza uma região\n", 8, " ")),
                Message.raw("/region flag").color(Color.WHITE).bold(true), Message.raw(" <region> [flag] [args]").color(Color.YELLOW).bold(true), Message.raw(" Gerencia flags\n")
        ));
    }

    public static void sendRegionData(final CommandContext context, final RegionData regionData, String beforeText) {
        context.sendMessage(Message.join(
                Message.raw("\n" + beforeText + " Informações:\n").color(Color.CYAN),
                Message.raw("ID: ").color(Color.WHITE).bold(true), Message.raw(regionData.getId().toString() + "\n").color(Color.PINK).bold(true),
                Message.raw("Nome: ").color(Color.WHITE).bold(true), Message.raw(regionData.getName() + "\n").color(Color.PINK).bold(true),
                Message.raw("Mundo: ").color(Color.WHITE).bold(true), Message.raw(regionData.getWorldName() + "\n").color(Color.PINK).bold(true),
                Message.raw("Tipo: ").color(Color.WHITE).bold(true), Message.raw(regionData.getType().getDetail() + "\n").color(Color.PINK).bold(true),
                Message.raw("Prioridade: ").color(Color.WHITE).bold(true), Message.raw(regionData.getPriority() + "\n").color(Color.PINK).bold(true),
                Message.raw("Área:\n").color(Color.WHITE).bold(true), ColorUtil.colorize(regionData.getBounds()).color(Color.PINK).bold(true)
        ));
    }
}
