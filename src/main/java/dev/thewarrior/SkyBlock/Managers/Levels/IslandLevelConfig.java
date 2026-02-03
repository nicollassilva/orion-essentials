package dev.thewarrior.SkyBlock.Managers.Levels;

public class IslandLevelConfig {
    private int level;

    private String displayName;
    private String icon;
    private String description;

    private double requiredPoints;

    private IslandLevelReward reward;

    public IslandLevelConfig(int level, String displayName, String icon, String description, double requiredPoints, IslandLevelReward reward) {
        this.level = level;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.requiredPoints = requiredPoints;
        this.reward = reward;
    }

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
