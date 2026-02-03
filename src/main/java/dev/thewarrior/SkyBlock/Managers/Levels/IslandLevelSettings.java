package dev.thewarrior.SkyBlock.Managers.Levels;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IslandLevelSettings {
    private CopyOnWriteArrayList<IslandLevelConfig> levels = new CopyOnWriteArrayList<>();

    public List<IslandLevelConfig> getLevels() {
        return levels;
    }

    public void setLevels(List<IslandLevelConfig> levels) {
        this.levels = new CopyOnWriteArrayList<>(levels);
    }
}
