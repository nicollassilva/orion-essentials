package dev.thewarrior.SkyBlock.Managers.Islands;

import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandLastVisitData;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class IslandData {
    private UUID id;
    private UUID ownerId;

    private String islandName;
    private String worldName;

    private String enterTitle;

    private int level = 1;
    private double experience = 0d;

    private IslandLastVisitData lastVisitData;

    private transient AtomicBoolean needsUpdate = new AtomicBoolean(false);

    public IslandData(UUID ownerId, String worldName) {
        this.id = UUID.randomUUID();

        this.ownerId = ownerId;
        this.worldName = worldName;
        this.islandName = "Default";

        this.needsUpdate.set(true);
    }

    public UUID getId() {
        return this.id;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public String getIslandName() {
        return this.islandName;
    }

    public String getEnterTitle() {
        return this.enterTitle;
    }

    public void setEnterTitle(String enterTitle) {
        this.enterTitle = enterTitle;
        this.needsUpdate.set(true);
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
        this.needsUpdate.set(true);
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.needsUpdate.set(true);
    }

    public double getExperience() {
        return this.experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
        this.needsUpdate.set(true);
    }

    public IslandLastVisitData getLastVisitData() {
        return this.lastVisitData;
    }

    public void setLastVisitData(IslandLastVisitData lastVisitData) {
        this.lastVisitData = lastVisitData;
        this.needsUpdate.set(true);
    }

    public boolean needsUpdate() {
        return this.needsUpdate.getAndSet(false);
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }
}
