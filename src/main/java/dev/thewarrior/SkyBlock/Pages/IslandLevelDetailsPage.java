package dev.thewarrior.SkyBlock.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelConfig;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelReward;
import dev.thewarrior.SkyBlock.Pages.Data.IslandLevelDetailsPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.List;

public class IslandLevelDetailsPage extends InteractiveCustomUIPage<IslandLevelDetailsPageData> {
    private final IslandLevelManager islandLevelManager;
    private final IslandsManager islandsManager;
    private final PlayerRef playerRef;
    private final IslandData islandData;

    public IslandLevelDetailsPage(@Nonnull PlayerRef playerRef, IslandsManager islandsManager, IslandLevelManager islandLevelManager, IslandData islandData) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandLevelDetailsPageData.CODEC);

        this.playerRef = playerRef;
        this.islandsManager = islandsManager;
        this.islandLevelManager = islandLevelManager;
        this.islandData = islandData;
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

            commandBuilder.set(selector + " #LevelLabel.Text", String.valueOf(level.getLevel()));
            commandBuilder.set(selector + " #TitleLabel.Text", level.getDisplayName());
            commandBuilder.set(selector + " #XPRequiredLabel.Text", "XP: " + level.getRequiredPoints());
            commandBuilder.set(selector + " #LevelIcon.ItemId", level.getIcon());
            commandBuilder.set(selector + " #DescriptionLabel.Text", level.getDescription());

            boolean isCompleted = level.getLevel() <= this.islandData.getLevel();

            if (isCompleted) {
                commandBuilder.set(selector + ".Background", "Common/Buttons/Tertiary.png");
                commandBuilder.set(selector + ".Background.Border", 16);

                commandBuilder.set(selector + " #LevelButton.Background", "Common/Buttons/Tertiary.png");
                commandBuilder.set(selector + " #LevelButton.Background.Border", 16);
                commandBuilder.set(selector + " #TitleLabel.Style.TextColor", "#7ed56f");
                commandBuilder.set(selector + " #XPRequiredLabel.Visible", false);
                commandBuilder.clear(selector + " #RewardsList");
                commandBuilder.append(selector + " #RewardsList", "Pages/SkyBlock/CompletedLabel.ui");
                continue;
            } else if(this.islandData.getLevel() + 1 == level.getLevel()) {
                commandBuilder.set(selector + ".Background", "Common/Buttons/Primary.png");
                commandBuilder.set(selector + ".Background.Border", 16);

                commandBuilder.set(selector + " #LevelButton.Background", "Common/Buttons/Primary.png");
                commandBuilder.set(selector + " #LevelButton.Background.Border", 16);
            } else {
                commandBuilder.set(selector + ".Background", "Common/Buttons/Disabled.png");
                commandBuilder.set(selector + ".Background.Border", 16);

                commandBuilder.set(selector + " #LevelButton.Background", "Common/Buttons/Disabled.png");
                commandBuilder.set(selector + " #LevelButton.Background.Border", 16);
            }

            final IslandLevelReward reward = level.getReward();

            if(reward == null) continue;

            if (reward.hasCoinReward()) {
                String rewardSelector = selector + " #RewardsList[0]";

                commandBuilder.append(selector + " #RewardsList", "Pages/SkyBlock/RewardItem.ui");
                commandBuilder.set(rewardSelector + " #ItemIcon.ItemId", "Ingredient_Powder_Boom");
                commandBuilder.set(rewardSelector + ".TooltipTextSpans", ColorUtil.colorize("&fRecompensa: &l&eMoedas\n&f&xQuantidade: &l&b" + reward.getCointAmount()));

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
                commandBuilder.set(rewardSelector + ".TooltipTextSpans", ColorUtil.colorize("&fRecompensa: &l&e" + reward.getItemId() + "\n&f&xQuantidade: &l&b" + reward.getItemAmount()));

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

        player.getPageManager().openCustomPage(ref, store, new IslandMenuPage(this.playerRef, this.islandsManager, this.islandLevelManager));
    }
}
