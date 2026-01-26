package dev.thewarrior.MiniGames.Gaming.Team;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {
    private final Map<String, GameTeam> teams;
    private final Map<UUID, String> playerTeams; // playerId -> teamId

    public TeamManager() {
        this.teams = new ConcurrentHashMap<>();
        this.playerTeams = new ConcurrentHashMap<>();
    }

    public GameTeam createTeam(String id, String displayName, int maxSize) {
        GameTeam team = new GameTeam(id, displayName, maxSize);
        teams.put(id, team);
        return team;
    }

    public boolean joinTeam(UUID playerId, String teamId) {
        GameTeam team = teams.get(teamId);
        if (team == null || team.isFull()) return false;

        // Remove do time anterior se houver
        leaveCurrentTeam(playerId);

        if (team.addMember(playerId)) {
            playerTeams.put(playerId, teamId);
            return true;
        }
        return false;
    }

    public void leaveCurrentTeam(UUID playerId) {
        String currentTeamId = playerTeams.remove(playerId);
        if (currentTeamId != null) {
            GameTeam team = teams.get(currentTeamId);
            if (team != null) {
                team.removeMember(playerId);
            }
        }
    }

    public Optional<GameTeam> getPlayerTeam(UUID playerId) {
        String teamId = playerTeams.get(playerId);
        return teamId != null ? Optional.ofNullable(teams.get(teamId)) : Optional.empty();
    }

    public GameTeam getSmallestTeam() {
        return teams.values().stream()
                .filter(t -> !t.isFull())
                .min((a, b) -> Integer.compare(a.getMemberCount(), b.getMemberCount()))
                .orElse(null);
    }

    public Collection<GameTeam> getTeams() {
        return teams.values();
    }

    public GameTeam getTeam(String id) {
        return teams.get(id);
    }

    public void clear() {
        teams.clear();
        playerTeams.clear();
    }
}

