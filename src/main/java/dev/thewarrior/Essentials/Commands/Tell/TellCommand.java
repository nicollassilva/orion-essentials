package dev.thewarrior.Essentials.Commands.Tell;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.Utils.PlayerUtils;
import dev.thewarrior.Essentials.Utils.StringUtils;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TellCommand extends AbstractPlayerCommand {
    private final Color color = new Color(214, 120, 206);
    private static final Map<UUID, UUID> lastMessagePartner = new ConcurrentHashMap<>();

    public TellCommand() {
        super("tell", "Envia uma mensagem privada para um jogador");

        requirePermission(PermissionUtil.getPermission("tells"));
        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        String rawInput = commandContext.getInputString();
        String[] parts = rawInput.split("\\s+", 3);

        if (parts.length < 3) {
            commandContext.sendMessage(Message.join(
                    Message.raw("\n- Como usar o comando ").color(Color.GREEN), Message.raw("/tell").color(Color.WHITE).bold(true), Message.raw(":\n").color(Color.GREEN),
                    Message.raw("Permite que você envie mensagens privadas para outro jogador online.\n\n").color(Color.LIGHT_GRAY).italic(true),
                    Message.raw("/tell ").color(Color.WHITE).bold(true), Message.raw("<player> <mensagem>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Envia uma mensagem privada\n", 8, " ")),
                    Message.raw("/tellon").color(Color.WHITE).bold(true), Message.raw(StringUtils.padLeft("Ativa suas mensagens privadas\n", 46, " ")),
                    Message.raw("/telloff").color(Color.WHITE).bold(true), Message.raw(StringUtils.padLeft("Desativa suas mensagens privadas\n", 45, " ")),
                    Message.raw("/r").color(Color.WHITE).bold(true), Message.raw(" <mensagem>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Responde à ultima mensagem\n", 29, " "))
            ));

            return;
        }

        final String targetName = parts[1];
        final String message = parts[2];

        PlayerRef player = PlayerUtils.findPlayer(targetName);

        if(player == null || !player.isValid()) {
            commandContext.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        if(playerRef.getUsername().equals(player.getUsername())) {
            commandContext.sendMessage(Messages.CANNOT_TELL_YOURSELF.color(Color.YELLOW));
            return;
        }

        final Ref<EntityStore> playerEntity = player.getReference();

        if(playerEntity == null || !playerEntity.isValid()) {
            commandContext.sendMessage(Messages.PLAYER_NOT_FOUND.color(Color.RED));
            return;
        }

        if(message.isEmpty()) return;

        playerRef.sendMessage(Message.raw("Para [" + player.getUsername() + "]: " + message).color(color));
        player.sendMessage(Message.raw("[" + playerRef.getUsername() + "]: " + message).color(color));

        lastMessagePartner.put(playerRef.getUuid(), player.getUuid());
        lastMessagePartner.put(player.getUuid(), playerRef.getUuid());
    }

    @Nullable
    public static UUID getLastMessagePartner(UUID playerId) {
        return lastMessagePartner.get(playerId);
    }

    public static void onPlayerQuit(UUID uuid) {
        lastMessagePartner.remove(uuid);
    }
}