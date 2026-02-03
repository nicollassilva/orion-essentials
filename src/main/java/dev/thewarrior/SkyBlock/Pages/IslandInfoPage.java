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
import com.hypixel.hytale.server.core.util.NotificationUtil;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelConfig;
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelSettings;
import dev.thewarrior.SkyBlock.Pages.Data.IslandInfoPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.List;

public class IslandInfoPage extends InteractiveCustomUIPage<IslandInfoPageData> {
    private final IslandsManager islandsManager;
    private final IslandLevelManager islandLevelManager;
    private final PlayerRef playerRef;
    private final IslandData islandData;

    public IslandInfoPage(@Nonnull PlayerRef playerRef, IslandData islandData, IslandsManager islandsManager, IslandLevelManager islandLevelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandInfoPageData.CODEC);

        this.playerRef = playerRef;
        this.islandData = islandData;
        this.islandsManager = islandsManager;
        this.islandLevelManager = islandLevelManager;
    }

    @Override
    public void build(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl UICommandBuilder commandBuilder,
            @NonNullDecl UIEventBuilder eventBuilder,
            @NonNullDecl Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/SkyBlock/IslandInfoPage.ui");

        this.updateIslandInfo(commandBuilder);

        this.bindMenuEvents(eventBuilder);
    }

    private void updateIslandInfo(UICommandBuilder commandBuilder) {
        commandBuilder.set("#IslandNameLabel.Text", this.islandData.getName());
        commandBuilder.set("#IslandTitleLabel.Text", this.islandData.getEnterTitle() != null ? this.islandData.getEnterTitle() : "Nenhum");

        IslandLevelSettings settings = this.islandLevelManager.getData();
        List<IslandLevelConfig> levels = settings.getLevels();
        int maxLevel = levels.size();
        commandBuilder.set("#IslandLevelLabel.Text", this.islandData.getLevel() + "/" + maxLevel);

        double currentExp = this.islandData.getExperience();
        int level = this.islandData.getLevel();
        double nextExp = level < maxLevel ? levels.get(level).getRequiredPoints() : currentExp;
        commandBuilder.set("#IslandExpLabel.Text", (int)currentExp + "/" + (int)nextExp + " XP");

        commandBuilder.set("#IslandSizeLabel.Text", "100x100"); // Placeholder
    }

    private void bindMenuEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#LevelInfoButton",
                EventData.of("Action", "OpenLevelInfo"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ExpandButton",
                EventData.of("Action", "ExpandIsland"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "ClosePage"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandInfoPageData data) {
        System.out.println("Handling IslandInfoPageData event: " + data.action);

        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        final boolean refIsValid = playerRef != null && playerRef.isValid();

        switch (data.action) {
            case "OpenLevelInfo" -> this.onOpenLevelInfo(ref, store, playerRef, refIsValid);
            case "ExpandIsland" -> {
                if (refIsValid) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&eExpandir ilha - Em breve!"));
                }
                this.onClose(ref, store, true);
            }
            case "ClosePage" -> this.onClose(ref, store, true);
            default -> this.onClose(ref, store, false);
        }
    }

    private void onOpenLevelInfo(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (!refIsValid) return;

        Player player = store.getComponent(ref, Player.getComponentType());

        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, new IslandLevelDetailsPage(playerRef, this.islandsManager, this.islandLevelManager, this.islandData));
        }
    }

    private void onClose(Ref<EntityStore> ref, Store<EntityStore> store, boolean backToSettings) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;

        if(backToSettings) {
            player.getPageManager().openCustomPage(ref, store, new IslandSettingsPage(this.playerRef, this.islandData, this.islandsManager, this.islandLevelManager));
        } else {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }
}
