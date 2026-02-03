package dev.thewarrior.SkyBlock.Managers;

import dev.thewarrior.Essentials.Managers.Composition.StorableManager;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelConfig;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelReward;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelSettings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IslandLevelManager extends StorableManager<IslandLevelSettings> {
    public IslandLevelManager(Path dataFolder) {
        super(dataFolder, "levels.json", IslandLevelSettings.class);
    }

    @Override
    public IslandLevelSettings createDefaultData() {
        IslandLevelSettings settings = new IslandLevelSettings();
        List<IslandLevelConfig> levels = new ArrayList<>();
        Random rand = new Random();

        for (int i = 1; i <= 30; i++) {
            String displayName = "Temporada do Nível " + i; // Placeholder para alteração posterior
            String icon = "Bench_Memories"; // Placeholder
            String description = "Descrição para o nível " + i; // Placeholder
            double requiredPoints = 100.0 * i * i; // Pontos necessários aumentam quadraticamente

            int coinAmount = rand.nextInt(1000) + 100; // Moedas entre 100 e 1099
            IslandLevelReward reward;
            if (rand.nextBoolean()) {
                // Recompensa com moedas e item
                int itemAmount = rand.nextInt(10) + 1; // 1 a 10 itens
                reward = new IslandLevelReward(coinAmount, "Rock_Stone_Cobble", itemAmount);
            } else {
                // Apenas moedas
                reward = new IslandLevelReward(coinAmount);
            }

            IslandLevelConfig config = new IslandLevelConfig(i, displayName, icon, description, requiredPoints, reward);
            levels.add(config);
        }

        settings.setLevels(levels);
        return settings;
    }
}
