package dev.thewarrior.SkyBlock.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelConfig;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelReward;
import dev.thewarrior.SkyBlock.Pages.Data.IslandLevelDetailsPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.List;

public class IslandLevelDetailsPage extends InteractiveCustomUIPage<IslandLevelDetailsPageData> {
    private final IslandLevelManager islandLevelManager;
    private final PlayerRef playerRef;

    public IslandLevelDetailsPage(@Nonnull PlayerRef playerRef, IslandLevelManager islandLevelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandLevelDetailsPageData.CODEC);

        this.playerRef = playerRef;
        this.islandLevelManager = islandLevelManager;
    }

    @Override
    public void build(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl UICommandBuilder commandBuilder,
            @NonNullDecl UIEventBuilder eventBuilder,
            @NonNullDecl Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/SkyBlock/IslandLevelDetailsPage.ui");

        this.populateLevelDetails(commandBuilder);

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "ClosePage"),
                false
        );
    }

    private void populateLevelDetails(UICommandBuilder commandBuilder) {
        List<IslandLevelConfig> levels = this.islandLevelManager.getData().getLevels();

        for (int i = 0; i < levels.size(); i++) {
            IslandLevelConfig level = levels.get(i);
            String selector = "#LevelList[" + i + "]";

            commandBuilder.append("#LevelList", "Pages/SkyBlock/IslandLevelEntry.ui");

            commandBuilder.set(selector + " #LevelLabel.Text", "Nível " + level.getLevel());
            commandBuilder.set(selector + " #TitleLabel.Text", level.getDisplayName());
            commandBuilder.set(selector + " #XPRequiredLabel.Text", "XP: " + level.getRequiredPoints());
            commandBuilder.set(selector + " #LevelIcon.ItemId", level.getIcon());
            commandBuilder.set(selector + " #DescriptionLabel.Text", level.getDescription());

            // Rewards
            IslandLevelReward reward = level.getReward();
            if (reward.hasCoinReward()) {
                String rewardSelector = selector + " #RewardsList[0]";
                commandBuilder.append(selector + " #RewardsList", "Pages/SkyBlock/RewardItem.ui");
                commandBuilder.set(rewardSelector + " #ItemIcon.ItemId", "Ingredient_Powder_Boom");
                if (reward.getCointAmount() > 1) {
                    commandBuilder.set(rewardSelector + " #QuantityLabel.Text", String.valueOf(reward.getCointAmount()));
                    commandBuilder.set(rewardSelector + " #QuantityLabel.Visible", true);
                } else {
                    commandBuilder.set(rewardSelector + " #QuantityLabel.Visible", false);
                }
            }

            if (reward.hasItemReward()) {
                int index = reward.hasCoinReward() ? 1 : 0;
                String rewardSelector = selector + " #RewardsList[" + index + "]";
                commandBuilder.append(selector + " #RewardsList", "Pages/SkyBlock/RewardItem.ui");
                commandBuilder.set(rewardSelector + " #ItemIcon.ItemId", reward.getItemId());
                if (reward.getItemAmount() > 1) {
                    commandBuilder.set(rewardSelector + " #QuantityLabel.Text", String.valueOf(reward.getItemAmount()));
                    commandBuilder.set(rewardSelector + " #QuantityLabel.Visible", true);
                } else {
                    commandBuilder.set(rewardSelector + " #QuantityLabel.Visible", false);
                }
            }
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandLevelDetailsPageData data) {
        switch (data.action) {
            default -> this.onClose(ref, store);
        }
    }

    private void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;

        player.getPageManager().setPage(ref, store, Page.None);
    }
}
