package dev.thewarrior.Commands.Tpa;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import dev.thewarrior.Managers.TpaManager;
import dev.thewarrior.Utils.ColorUtil;
import dev.thewarrior.Utils.SoundsUtil;
import dev.thewarrior.Utils.StringUtils;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.awt.*;

public class TpaCommand extends AbstractPlayerCommand {
    private final TpaManager tpaManager;
    private final RequiredArg<PlayerRef> targetArg;

    public TpaCommand(@Nonnull TpaManager tpaManager) {
        super("tpa", "Solicita teletransporte para outro jogador.");

        this.tpaManager = tpaManager;
        this.targetArg = withRequiredArg("player", "Player alvo", ArgTypes.PLAYER_REF);

        requirePermission("multicommands.tpa");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext context,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        PlayerRef target = context.get(targetArg);

        if (target == null) {
            playerRef.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }
//
//        if (target.getUuid().equals(playerRef.getUuid())) {
//            playerRef.sendMessage(Messages.CANNOT_TELEPORT_YOURSELF.color(Color.YELLOW));
//            return;
//        }

        boolean created = tpaManager.createRequest(playerRef, target);

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
}

