package dev.thewarrior.MiniGames.Commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class LeaveGameCommand extends AbstractPlayerCommand {
    private final GameManager gameManager;

    public LeaveGameCommand(GameManager gameManager) {
        super("Sai do jogo ou fila atual.");
        this.gameManager = gameManager;
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef player,
                           @NonNullDecl World world) {

        gameManager.leaveQueue(player.getUuid());
        gameManager.leaveGame(player.getUuid());

        player.sendMessage("§eVocê saiu do jogo/fila.");
    }
}
