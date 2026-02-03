package dev.thewarrior.SkyBlock.Managers.Levels;

public class IslandLevelConfig {
    private int level;

    private String displayName;
    private String icon;
    private String description;

    private double requiredPoints;

    private IslandLevelReward reward;

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public double getRequiredPoints() {
        return requiredPoints;
    }

    public IslandLevelReward getReward() {
        return reward;
    }
}
