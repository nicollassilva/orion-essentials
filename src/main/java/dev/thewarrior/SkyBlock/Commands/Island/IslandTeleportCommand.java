package dev.thewarrior.SkyBlock.Commands.Island;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.TeleportManager;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;

public class IslandTeleportCommand extends AbstractPlayerCommand {
    private final IslandsManager islandsManager;
    private final TeleportManager teleportManager;

    public IslandTeleportCommand(final IslandsManager islandsManager, final TeleportManager teleportManager) {
        super("teleport", "Teleporta o jogador para sua ilha no SkyBlock");

        this.islandsManager = islandsManager;
        this.teleportManager = teleportManager;

        this.addAliases("tp", "ir", "teleportar");

        this.setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
       String input = commandContext.getInputString();
       String[] parts = input.split("\\s+", 4); // [command, tp, islandName, owner]

        if (parts.length < 2) {
            commandContext.sendMessage(ColorUtil.colorize("&cUso incorreto! Use: &f/island tp <nome_da_ilha> [dono?]&c para teleportar para uma ilha."));
            return;
        }

        String islandName = parts.length > 2 ? parts[2] : "Default";
        UUID ownerId = playerRef.getUuid();

        if(parts.length > 3 && !parts[3].equals(playerRef.getUsername())) {
            final PlayerRef player = Universe.get().getPlayerByUsername(parts[3], NameMatching.EXACT_IGNORE_CASE);

            if(player == null || !player.isValid()) {
                commandContext.sendMessage(ColorUtil.colorize("&cJogador não encontrado: &f" + parts[3]));
                return;
            }

            ownerId = player.getUuid();
        }

        final IslandData islandData = this.islandsManager.getIslandByNameAndOwner(islandName, ownerId);

        if(islandData == null) {
            final Message message = ownerId.equals(playerRef.getUuid())
                    ? ColorUtil.colorize("&cVocê não possui uma ilha com o nome: &f" + islandName)
                    : ColorUtil.colorize("&cO jogador não possui uma ilha com o nome: &f" + islandName);

            commandContext.sendMessage(message);
            return;
        }

        this.teleportManager.queueTeleport(
                playerRef, ref, store,
                playerRef.getTransform().getPosition(),
                islandData.getWorldName(), 2.5, 109, -2.6, -3.12f, 0f,
                ColorUtil.colorize("&aTeleportando para a ilha &f" + islandData.getName() + "&a" + (ownerId.equals(playerRef.getUuid()) ? "" : " do jogador &f" + parts[3]) + "&a...")
        );
    }
}
