package dev.thewarrior.MiniGames.Gaming.Handler.Impl;

import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Handler.GameHandler;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Gaming.Team.TeamManager;

import java.util.UUID;

public class BedWarsHandler extends GameHandler {
    private static final String KEY_TEAM_MANAGER = "team_manager";
    private static final String KEY_BEDS_STATUS = "beds_status";

    public BedWarsHandler() {
        super(GameType.BED_WARS);
    }

    @Override
    public void onGameCreate(Game game) {
        TeamManager teamManager = new TeamManager();
        teamManager.createTeam("red", "Red", 4);
        teamManager.createTeam("blue", "Blue", 4);
        game.getData().set(KEY_TEAM_MANAGER, teamManager);
    }

    @Override
    public void onCountdownTick(Game game, int secondsRemaining) {
    }

    @Override
    public void onGameStart(Game game) {
        TeamManager teams = game.getData().get(KEY_TEAM_MANAGER);
        if (teams == null) return;

        // Distribui jogadores nos times
        for (UUID playerId : game.getPlayers()) {
            var smallestTeam = teams.getSmallestTeam();
            if (smallestTeam != null) {
                teams.joinTeam(playerId, smallestTeam.getId());
            }
        }
    }

    @Override
    public void onGameTick(Game game) {
        // Verificar camas destruídas, generators, etc
    }

    @Override
    public void onGameEnd(Game game) {
        TeamManager teams = game.getData().get(KEY_TEAM_MANAGER);
        if (teams != null) {
            teams.clear();
        }
    }

    @Override
    public void onPlayerJoin(Game game, UUID playerId) {
        // Se o jogo já começou, adiciona ao time mais vazio
        TeamManager teams = game.getData().get(KEY_TEAM_MANAGER);
        if (teams != null && game.getState().isActive()) {
            var smallestTeam = teams.getSmallestTeam();
            if (smallestTeam != null) {
                teams.joinTeam(playerId, smallestTeam.getId());
            }
        }
    }

    @Override
    public void onPlayerLeave(Game game, UUID playerId) {
        TeamManager teams = game.getData().get(KEY_TEAM_MANAGER);
        if (teams != null) {
            teams.leaveCurrentTeam(playerId);
        }
    }
}

