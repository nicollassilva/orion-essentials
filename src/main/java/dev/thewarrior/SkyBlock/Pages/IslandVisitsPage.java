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
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandLastVisitData;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Pages.Data.IslandVisitsPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class IslandVisitsPage extends InteractiveCustomUIPage<IslandVisitsPageData> {
    private final PlayerRef playerRef;

    private final IslandData islandData;
    private final IslandsManager islandsManager;
    private final IslandLevelManager islandLevelManager;

    public IslandVisitsPage(
            @Nonnull PlayerRef playerRef,
            IslandData islandData,
            IslandsManager islandsManager,
            IslandLevelManager islandLevelManager
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandVisitsPageData.CODEC);

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
        commandBuilder.append("Pages/SkyBlock/IslandVisitsPage.ui");

        this.updateVisits(commandBuilder);

        this.bindMenuEvents(eventBuilder);
    }

    private void updateVisits(UICommandBuilder commandBuilder) {
        var visits = this.islandData.getLastVisitDataList();

        if (visits == null || visits.isEmpty()) {
            commandBuilder.clear("#VisitContainer");
            commandBuilder.set("#VisitLabel.Visible", true);
            return;
        }

        commandBuilder.set("#VisitLabel.Visible", false);
        int count = 0;

        for (IslandLastVisitData visit : visits.reversed()) {
            if (visit == null) continue;

            String visitSelector = "#VisitContainer[" + count++ + "] ";

            commandBuilder.append("#VisitContainer", "Pages/SkyBlock/IslandVisitEntry.ui");

            commandBuilder.set(visitSelector + "#NameLabel.Text", visit.getUsername());
            commandBuilder.set(visitSelector + "#TimeLabel.Text", this.formatTimeAgo(visit.getTime()));
        }
    }

    private void bindMenuEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ClearButton",
                EventData.of("Action", "ClearVisits"),
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
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandVisitsPageData data) {
        System.out.println("Handling IslandVisitsPageData event: " + data.action);

        switch (data.action) {
            case "ClearVisits" -> this.onClearVisits();
            case "ClosePage" -> this.onClose(ref, store);
            default -> this.onClose(ref, store);
        }
    }

    private void onClearVisits() {
        if (this.islandData.getLastVisitDataList() == null) return;

        this.islandData.clearLastVisitsData();
        this.islandsManager.save(this.islandData);

        UICommandBuilder commandBuilder = new UICommandBuilder();

        this.updateVisits(commandBuilder);
        this.sendUpdate(commandBuilder);
    }

    private void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, new IslandSettingsPage(this.playerRef, this.islandData, this.islandsManager, this.islandLevelManager));
        }
    }

    private String formatTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) {
            return years + " ano" + (years > 1 ? "s" : "") + " atrás";
        } else if (months > 0) {
            return months + " mês" + (months > 1 ? "es" : "") + " atrás";
        } else if (weeks > 0) {
            return weeks + " semana" + (weeks > 1 ? "s" : "") + " atrás";
        } else if (days > 0) {
            return days + " dia" + (days > 1 ? "s" : "") + " atrás";
        } else if (hours > 0) {
            return hours + " hora" + (hours > 1 ? "s" : "") + " atrás";
        } else if (minutes > 0) {
            return minutes + " minuto" + (minutes > 1 ? "s" : "") + " atrás";
        } else {
            return "agora";
        }
    }
}
