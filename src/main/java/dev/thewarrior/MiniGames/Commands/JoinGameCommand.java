package dev.thewarrior.MiniGames.Commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Enums.JoinResult;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class JoinGameCommand extends AbstractPlayerCommand {
    private final GameManager gameManager;
    private final RequiredArg<String> gameArg;

    public JoinGameCommand(GameManager gameManager) {
        super("Entra na fila de um minigame.");
        this.gameManager = gameManager;
        this.gameArg = this.withRequiredArg("game", "Nome do jogo (tntrun, bedwars, skywars, spleef)", ArgTypes.STRING);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef player,
                           @NonNullDecl World world) {

        String gameName = ctx.get(this.gameArg).toLowerCase();

        GameType type = switch (gameName) {
            case "tntrun", "tnt" -> GameType.TNT_RUN;
            case "bedwars", "bw" -> GameType.BED_WARS;
            case "skywars", "sw" -> GameType.SKY_WARS;
            case "spleef" -> GameType.SPLEEF;
            default -> null;
        };

        if (type == null) {
            player.sendMessage("§cJogo não encontrado! Use: tntrun, bedwars, skywars, spleef");
            return;
        }

        JoinResult result = gameManager.joinQueue(player.getUuid(), type);

        switch (result) {
            case SUCCESS -> player.sendMessage("§aVocê entrou na fila do " + type.getId() + "!");
            case ALREADY_IN_GAME -> player.sendMessage("§cVocê já está em um jogo ou fila!");
            case GAME_FULL -> player.sendMessage("§cO jogo está cheio!");
            default -> player.sendMessage("§cNão foi possível entrar no jogo.");
        }
    }
}
