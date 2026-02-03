package dev.thewarrior.SkyBlock.Managers.Levels;

public class IslandLevelReward {
    private int cointAmount;

    private String itemId;
    private int itemAmount;

    public IslandLevelReward(int cointAmount, String itemId, int itemAmount) {
        this.cointAmount = cointAmount;
        this.itemId = itemId;
        this.itemAmount = itemAmount;
    }

    public IslandLevelReward(int cointAmount) {
        this.cointAmount = cointAmount;
        this.itemId = null;
        this.itemAmount = 0;
    }

    public IslandLevelReward(String itemId, int itemAmount) {
        this.cointAmount = 0;
        this.itemId = itemId;
        this.itemAmount = itemAmount;
    }

    public int getCointAmount() {
        return cointAmount;
    }

    public String getItemId() {
        return itemId;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public boolean hasCoinReward() {
        return cointAmount > 0;
    }

    public boolean hasItemReward() {
        return itemId != null && !itemId.isEmpty() && itemAmount > 0;
    }
}
