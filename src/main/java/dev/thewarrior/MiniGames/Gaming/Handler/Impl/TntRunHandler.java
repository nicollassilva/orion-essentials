package dev.thewarrior.MiniGames.Gaming.Handler.Impl;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Handler.GameHandler;
import dev.thewarrior.MiniGames.Gaming.Model.Game;

import java.util.List;
import java.util.UUID;

public class TntRunHandler extends GameHandler {

    // Spawns do lobby (onde jogadores esperam antes de começar)
    private static final Vector3d LOBBY_SPAWN = new Vector3d(0, 100, 0);

    // Spawns da arena (onde jogadores são teleportados ao iniciar)
    private static final List<Vector3d> ARENA_SPAWNS = List.of(
        new Vector3d(10, 50, 10),
        new Vector3d(-10, 50, 10),
        new Vector3d(10, 50, -10),
        new Vector3d(-10, 50, -10),
        new Vector3d(0, 50, 15),
        new Vector3d(0, 50, -15),
        new Vector3d(15, 50, 0),
        new Vector3d(-15, 50, 0)
    );

    public TntRunHandler() {
        super(GameType.TNT_RUN);
    }

    @Override
    public void onGameCreate(Game game) {
        // Arena criada - preparar mapa, resetar blocos
        // game.getData().set("world", worldInstance);
    }

    @Override
    public void onPlayerJoin(Game game, UUID playerId) {
        // Jogador entrou -> teleporta pro lobby da arena
        PlayerRef player = Universe.get().getPlayer(playerId);
        if (player == null) return;

        teleportPlayer(player, LOBBY_SPAWN);
        broadcastToGame(game, "§e" + player.getUsername() + " §fentrou! §7(" + game.getPlayerCount() + "/" + game.getType().getMaxPlayers() + ")");
    }

    @Override
    public void onCountdownTick(Game game, int secondsRemaining) {
        // Countdown tick - exibir mensagem
        if (secondsRemaining <= 5 || secondsRemaining == 10) {
            broadcastToGame(game, "§eO jogo começa em §c" + secondsRemaining + " §esegundos!");
        }
    }

    @Override
    public void onGameStart(Game game) {
        // Jogo iniciou - teleportar todos para arena
        int spawnIndex = 0;
        for (UUID playerId : game.getPlayers()) {
            PlayerRef player = Universe.get().getPlayer(playerId);
            if (player == null) continue;

            Vector3d spawn = ARENA_SPAWNS.get(spawnIndex % ARENA_SPAWNS.size());
            teleportPlayer(player, spawn);
            spawnIndex++;
        }

        broadcastToGame(game, "§a§lO JOGO COMEÇOU! §fNão caia!");
    }

    @Override
    public void onGameTick(Game game) {
        // Lógica do jogo rodando (20x por segundo)
        // - Verificar blocos pisados
        // - Remover blocos com delay
        // - Verificar jogadores que caíram
    }

    @Override
    public void onGameEnd(Game game) {
        // Jogo terminou
        broadcastToGame(game, "§6§lFIM DE JOGO!");

        // Teleportar todos de volta ao lobby principal
        for (UUID playerId : game.getPlayers()) {
            PlayerRef player = Universe.get().getPlayer(playerId);
            if (player != null) {
                // teleportToMainLobby(player);
            }
        }
    }

    @Override
    public void onPlayerLeave(Game game, UUID playerId) {
        PlayerRef player = Universe.get().getPlayer(playerId);
        String name = player != null ? player.getUsername() : "Jogador";
        broadcastToGame(game, "§c" + name + " §fsaiu do jogo.");
    }

    // --- Helpers ---

    private void teleportPlayer(PlayerRef player, Vector3d position) {
        // player.teleport(position);
    }

    private void broadcastToGame(Game game, String message) {
        for (UUID playerId : game.getPlayers()) {
            PlayerRef player = Universe.get().getPlayer(playerId);
            if (player != null) {
                // player.sendMessage(message);
            }
        }
    }
}
