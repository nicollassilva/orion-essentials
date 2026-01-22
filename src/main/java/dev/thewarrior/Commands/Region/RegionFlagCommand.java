package dev.thewarrior.Commands.Region;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.Data.Region.Flag.RegionFlag;
import dev.thewarrior.Managers.Data.Region.Flag.RegionFlagData;
import dev.thewarrior.Managers.RegionManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;
import java.util.Map;

public class RegionFlagCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> regionName;
    private final RegionManager regionManager;

    public RegionFlagCommand(RegionManager regionManager) {
        super("flag", "Gerencia as flags de uma região.");

        this.regionManager = regionManager;
        this.regionName = this.withRequiredArg("region", "Nome da região", ArgTypes.STRING);

        this.setAllowsExtraArguments(true);
        requirePermission("multicommands.region.flag");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final String regionNameValue = this.regionName.get(commandContext);
        final RegionData regionData = this.regionManager.getRegionByName(regionNameValue);

        if (regionData == null) {
            commandContext.sendMessage(Message.raw("[ERRO] A região '" + regionNameValue + "' não foi encontrada.").color(Color.RED));
            return;
        }

        String rawInput = commandContext.getInputString();
        String[] parts = rawInput.split("\\s+");

        if (parts.length <= 3) {
            showAllFlags(commandContext, regionData);
            return;
        }

        String flagName = parts[3];
        RegionFlag flag = RegionFlag.fromString(flagName);

        if (flag == null || flag == RegionFlag.NOT_FOUND) {
            commandContext.sendMessage(Message.raw("[ERRO] Flag '" + flagName + "' não encontrada.").color(Color.RED));
            showAvailableFlags(commandContext);
            return;
        }

        if (parts.length == 4) {
            showFlagDetails(commandContext, regionData, flag);
            return;
        }

        RegionFlagData flags = regionData.getFlags();

        switch (flag.getType()) {
            case MAPPED -> handleMappedFlag(commandContext, parts, flag, flags);
            case MESSAGE -> handleMessageFlag(commandContext, parts, flag, flags);
            case BOOLEAN -> handleBooleanFlag(commandContext, parts, flag, flags);
            default -> commandContext.sendMessage(Message.raw("[ERRO] Tipo de flag desconhecido.").color(Color.RED));
        }

        this.regionManager.save();
    }

    private void handleMappedFlag(CommandContext ctx, String[] parts, RegionFlag flag, RegionFlagData flags) {
        if (parts.length < 5) {
            ctx.sendMessage(Message.raw("[AVISO] Uso: /region flag <região> " + flag.getName() + " <key> <true|false|remove>").color(Color.YELLOW));
            ctx.sendMessage(Message.raw("Use '*' como key para definir regra padrão (wildcard).").color(Color.LIGHT_GRAY));
            return;
        }

        String key = parts[4];

        if (parts.length < 6) {
            final Boolean value = flags.checkMappedPermission(flag.getName(), key);

            if (value == null) {
                ctx.sendMessage(Message.raw("Flag '" + flag.getName() + "' não tem valor definido para '" + key + "'.").color(Color.YELLOW));
            } else {
                ctx.sendMessage(Message.raw("Flag '" + flag.getName() + "' [" + key + "] = " + value).color(Color.GREEN));
            }
            return;
        }

        String valueStr = parts[5].toLowerCase();

        if (valueStr.equals("remove") || valueStr.equals("delete") || valueStr.equals("clear")) {
            flags.removeMappedValue(flag.getName(), key);
            ctx.sendMessage(Message.raw("Valor '" + key + "' removido da flag '" + flag.getName() + "'.").color(Color.GREEN));
        } else {
            boolean value = valueStr.equals("true") || valueStr.equals("1") || valueStr.equals("yes") || valueStr.equals("allow");
            flags.setMappedValue(flag.getName(), key, value);
            ctx.sendMessage(Message.raw("Flag '" + flag.getName() + "' [" + key + "] = " + value).color(Color.GREEN));
        }
    }

    private void handleMessageFlag(CommandContext ctx, String[] parts, RegionFlag flag, RegionFlagData flags) {
        StringBuilder message = new StringBuilder();

        for (int i = 4; i < parts.length; i++) {
            if (i > 4) message.append(" ");

            message.append(parts[i]);
        }

        String messageStr = message.toString().trim();

        if (messageStr.equalsIgnoreCase("remove") || messageStr.equalsIgnoreCase("delete") || messageStr.equalsIgnoreCase("clear")) {
            flags.removeMessage(flag.getName());
            ctx.sendMessage(Message.raw("Mensagem da flag '" + flag.getName() + "' removida.").color(Color.GREEN));
        } else {
            flags.setMessage(flag.getName(), messageStr);
            ctx.sendMessage(Message.raw("Flag '" + flag.getName() + "' = \"" + messageStr + "\"").color(Color.GREEN));
        }
    }


    /**
     * Manipula flags do tipo BOOLEAN.
     * Formato: /region flag <região> <flag> <true|false>
     * Exemplo: /region flag spawn pvp false
     */
    private void handleBooleanFlag(CommandContext ctx, String[] parts, RegionFlag flag, RegionFlagData flags) {
        String valueStr = parts[4].toLowerCase();

        if (valueStr.equals("remove") || valueStr.equals("delete") || valueStr.equals("clear")) {
            flags.removeBoolean(flag.getName());
            ctx.sendMessage(Message.raw("Flag '" + flag.getName() + "' removida.").color(Color.GREEN));
        } else {
            boolean value = valueStr.equals("true") || valueStr.equals("1") || valueStr.equals("yes") || valueStr.equals("on");
            flags.setBoolean(flag.getName(), value);
            ctx.sendMessage(Message.raw("Flag '" + flag.getName() + "' = " + value).color(Color.GREEN));
        }
    }

    /**
     * Mostra todas as flags configuradas de uma região.
     */
    private void showAllFlags(CommandContext ctx, RegionData regionData) {
        RegionFlagData flags = regionData.getFlags();

        ctx.sendMessage(Message.raw("\n=== Flags da região '" + regionData.getName() + "' ===\n").color(Color.CYAN).bold(true));

        if (flags.isEmpty()) {
            ctx.sendMessage(Message.raw("Nenhuma flag configurada.").color(Color.LIGHT_GRAY).italic(true));
            showAvailableFlags(ctx);
            return;
        }

        // Mostra flags MAPPED
        for (Map.Entry<String, Map<String, Boolean>> entry : flags.getMappedFlags().entrySet()) {
            ctx.sendMessage(Message.raw("• " + entry.getKey() + " (MAPPED):").color(Color.GREEN).bold(true));
            for (Map.Entry<String, Boolean> subEntry : entry.getValue().entrySet()) {
                ctx.sendMessage(Message.raw("    " + subEntry.getKey() + " = " + subEntry.getValue()).color(Color.WHITE));
            }
        }

        // Mostra flags MESSAGE
        for (Map.Entry<String, String> entry : flags.getMessageFlags().entrySet()) {
            ctx.sendMessage(Message.raw("• " + entry.getKey() + " (MESSAGE): ").color(Color.GREEN).bold(true));
            ctx.sendMessage(Message.raw("    \"" + entry.getValue() + "\"").color(Color.WHITE));
        }

        // Mostra flags BOOLEAN
        for (Map.Entry<String, Boolean> entry : flags.getBooleanFlags().entrySet()) {
            ctx.sendMessage(Message.raw("• " + entry.getKey() + " (BOOLEAN): " + entry.getValue()).color(Color.GREEN).bold(true));
        }
    }

    /**
     * Mostra detalhes de uma flag específica.
     */
    private void showFlagDetails(CommandContext ctx, RegionData regionData, RegionFlag flag) {
        RegionFlagData flags = regionData.getFlags();

        ctx.sendMessage(Message.raw("\n=== Flag '" + flag.getName() + "' ===").color(Color.CYAN).bold(true));
        ctx.sendMessage(Message.raw("Descrição: " + flag.getDescription()).color(Color.LIGHT_GRAY).italic(true));
        ctx.sendMessage(Message.raw("Tipo: " + flag.getType().name() + "\n").color(Color.LIGHT_GRAY));

        switch (flag.getType()) {
            case MAPPED -> {
                Map<String, Boolean> values = flags.getMappedValues(flag.getName());
                if (values == null || values.isEmpty()) {
                    ctx.sendMessage(Message.raw("Nenhum valor configurado.").color(Color.YELLOW));
                } else {
                    for (Map.Entry<String, Boolean> entry : values.entrySet()) {
                        ctx.sendMessage(Message.raw("  " + entry.getKey() + " = " + entry.getValue()).color(Color.WHITE));
                    }
                }
            }
            case MESSAGE -> {
                String message = flags.getMessage(flag.getName());
                if (message == null) {
                    ctx.sendMessage(Message.raw("Nenhuma mensagem configurada.").color(Color.YELLOW));
                } else {
                    ctx.sendMessage(Message.raw("  \"" + message + "\"").color(Color.WHITE));
                }
            }
            case BOOLEAN -> {
                Boolean value = flags.getBoolean(flag.getName());
                if (value == null) {
                    ctx.sendMessage(Message.raw("Não configurado.").color(Color.YELLOW));
                } else {
                    ctx.sendMessage(Message.raw("  " + value).color(Color.WHITE));
                }
            }
        }
    }

    /**
     * Mostra todas as flags disponíveis.
     */
    private void showAvailableFlags(CommandContext ctx) {
        ctx.sendMessage(Message.raw("\nFlags disponíveis:").color(Color.YELLOW));

        for (RegionFlag flag : RegionFlag.getValidFlags()) {
            ctx.sendMessage(Message.join(
                    Message.raw("  • " + flag.getName()).color(Color.WHITE),
                    Message.raw(" - " + flag.getDescription()).color(Color.LIGHT_GRAY)
            ));
        }
    }
}

