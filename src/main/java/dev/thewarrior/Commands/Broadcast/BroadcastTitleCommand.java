package dev.thewarrior.Commands.Broadcast;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import dev.thewarrior.Utils.ColorUtil;
import dev.thewarrior.Utils.PermissionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class BroadcastTitleCommand extends AbstractPlayerCommand {
    public BroadcastTitleCommand() {
        super("title", "Envia um titulo para todos os jogadores online");

        requirePermission(PermissionUtil.getPermission("broadcast.title"));
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
        String[] parts = rawInput.split("\\s+", 3); // [command, type, title]

        if(parts.length < 3) return;

        String message = parts[2].trim();
        float duration = 4.0F;

        try {
            duration = Float.parseFloat(message.split("\\s+", 2)[0]);

            if(duration < -1.0F) {
                playerRef.sendMessage(ColorUtil.colorize("&cA duração mínima é de -1 segundo. (Desaparece rápido, sem animação)"));
                return;
            } else if(duration > 30.0F) {
                playerRef.sendMessage(ColorUtil.colorize("&cA duração máxima é de 30 segundos."));
                return;
            }

            if(message.contains(" ")) {
                message = message.split("\\s+", 2)[1].trim();
            } else {
                message = "";
            }
        } catch (NumberFormatException ignored) {}

        if(message.isEmpty() || message.length() > 51) {
            playerRef.sendMessage(ColorUtil.colorize("&cA mensagem deve ter entre 1 e 51 caracteres."));
            return;
        }

        for (final PlayerRef player : Universe.get().getPlayers()) {
            if(player == null || !player.isValid()) continue;

            EventTitleUtil.showEventTitleToPlayer(
                    player,
                    Message.raw(message),
                    Message.raw("Mensagem do Servidor"),
                    true,
                    null,
                    duration,
                    1.0F,
                    1.0F
            );
        }
    }
}