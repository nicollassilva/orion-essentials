package dev.thewarrior.MiniGames.Gaming.Team;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameTeam {
    private final String id;
    private final String displayName;
    private final int maxSize;
    private final Set<UUID> members;
    private final AtomicInteger score;

    public GameTeam(String id, String displayName, int maxSize) {
        this.id = id;
        this.displayName = displayName;
        this.maxSize = maxSize;
        this.members = ConcurrentHashMap.newKeySet();
        this.score = new AtomicInteger(0);
    }

    public boolean addMember(UUID playerId) {
        if (members.size() >= maxSize) return false;
        return members.add(playerId);
    }

    public boolean removeMember(UUID playerId) {
        return members.remove(playerId);
    }

    public boolean hasMember(UUID playerId) {
        return members.contains(playerId);
    }

    public int getMemberCount() {
        return members.size();
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public void addScore(int amount) {
        score.addAndGet(amount);
    }

    public int getScore() {
        return score.get();
    }

    public void resetScore() {
        score.set(0);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }
}

