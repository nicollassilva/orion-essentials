package dev.thewarrior.Commands.Tpa;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Components.PlayerCommandComponent;
import dev.thewarrior.Managers.TpaManager;
import dev.thewarrior.MultiCommands;
import dev.thewarrior.Utils.SoundsUtil;
import dev.thewarrior.Utils.StringUtils;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.awt.*;

public class TpaCommand extends AbstractPlayerCommand {
    private final TpaManager tpaManager;

    public TpaCommand(@Nonnull TpaManager tpaManager) {
        super("tpa", "Solicita teleporte para outro jogador.");

        this.tpaManager = tpaManager;

        requirePermission("multicommands.tpa");
        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext context,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final String input = context.getInputString();
        final String[] parts = input.split("\\s+", 2);
        final String targetName = parts.length > 1 ? parts[1] : null;

        if(targetName == null || targetName.isEmpty()) {
            showUsage(playerRef);
            return;
        }

        PlayerRef target = Universe.get().getPlayer(targetName, NameMatching.EXACT_IGNORE_CASE);

        if (target == null) {
            playerRef.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        Ref<EntityStore> targetRef = target.getReference();

        if(targetRef == null || !targetRef.isValid()) {
            playerRef.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

//        if (target.getUuid().equals(playerRef.getUuid())) {
//            playerRef.sendMessage(Messages.CANNOT_TELEPORT_YOURSELF.color(Color.YELLOW));
//            return;
//        }

        Store<EntityStore> targetStore = targetRef.getStore();
        PlayerCommandComponent targetData = targetStore.getComponent(targetRef, MultiCommands.PlayerDataComponent);

        if (targetData == null) {
            playerRef.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        if(targetData.isTpaOff()) {
            playerRef.sendMessage(Message.raw(
                    String.format(Messages.COMMAND_TPA_TARGET_DISABLED, target.getUsername())
            ).color(Color.YELLOW));
            return;
        }

        boolean created = this.tpaManager.createRequest(playerRef, target);

//        if (!created) {
//            playerRef.sendMessage(Message.raw(String.format(Messages.COMMAND_TPA_FAILED, target.getUsername())).color(Color.YELLOW));
//            return;
//        }

        playerRef.sendMessage(Message.raw(String.format(Messages.COMMAND_TPA_SUCCESS, target.getUsername())).color(Color.GREEN));

        SoundsUtil.playSound(target, "SFX_Alchemy_Bench_Open");

        target.sendMessage(Message.join(
                Message.raw("-".repeat(46) + "\n").color(Color.LIGHT_GRAY).bold(true),
                Message.raw("  » ").color(Color.ORANGE).bold(true),
                Message.raw(playerRef.getUsername() + " ").color(Color.YELLOW),
                Messages.COMMAND_TPA_TARGET_SUCCESS.color(Color.LIGHT_GRAY),
                Message.raw("\n "),
                Message.raw(StringUtils.padLeft("/tpaccept ", 18, " ")).color(Color.GREEN),
                Message.raw(" ou  ").color(Color.LIGHT_GRAY),
                Message.raw("/tpadeny").color(Color.RED),
                Message.raw("\n" + "-".repeat(46)).color(Color.LIGHT_GRAY).bold(true)
        ));
    }

    public static void showUsage(PlayerRef playerRef) {
        playerRef.sendMessage(Message.join(
                Message.raw("\n- Como usar o comando ").color(Color.GREEN), Message.raw("/tpa").color(Color.WHITE).bold(true), Message.raw(":\n").color(Color.GREEN),
                Message.raw("Permite que você teleporte para outro jogador online.\n\n").color(Color.LIGHT_GRAY).italic(true),
                Message.raw("/tpa ").color(Color.WHITE).bold(true), Message.raw(" <player>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Envia um pedido de teleporte\n", 24, " ")),
                Message.raw("/tpaccept").color(Color.WHITE).bold(true), Message.raw(" <player?>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Aceita um pedido de teleporte\n", 13, " ")),
                Message.raw("/tpadeny").color(Color.WHITE).bold(true), Message.raw(" <player?>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Recusa um pedido de teleporte\n", 14, " ")),
                Message.raw("/tpaoff").color(Color.WHITE).bold(true), Message.raw(StringUtils.padLeft("Desativa pedidos de teleporte\n", 36, " ")),
                Message.raw("/tpaon").color(Color.WHITE).bold(true), Message.raw(StringUtils.padLeft("Ativa pedidos de teleporte\n", 37, " "))
        ));
    }
}

