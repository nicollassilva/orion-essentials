package dev.thewarrior.SkyBlock.Managers.Islands.Data;

public class IslandLastVisitData {
    private long time;
    private String username;

    public long getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    public IslandLastVisitData(String username) {
        this.time = System.currentTimeMillis();
        this.username = username;
    }
}
