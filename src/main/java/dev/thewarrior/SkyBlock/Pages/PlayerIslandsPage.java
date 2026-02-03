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
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Pages.Data.PlayerIslandsPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.List;

public class PlayerIslandsPage extends InteractiveCustomUIPage<PlayerIslandsPageData> {
    private final IslandsManager islandsManager;
    private final IslandLevelManager islandLevelManager;
    private final PlayerRef playerRef;

    public PlayerIslandsPage(@Nonnull PlayerRef playerRef, IslandsManager islandsManager, IslandLevelManager islandLevelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, PlayerIslandsPageData.CODEC);

        this.playerRef = playerRef;
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
        commandBuilder.append("Pages/SkyBlock/PlayerIslandsPage.ui");

        this.updateIslands(commandBuilder);

        this.bindMenuEvents(eventBuilder);
    }

    private void updateIslands(UICommandBuilder commandBuilder) {
        List<IslandData> islands = this.islandsManager.getIslandsForPlayer(this.playerRef.getUuid());

        for (int i = 0; i < 9; i++) {
            if (i < islands.size()) {
                IslandData island = islands.get(i);
                commandBuilder.set("#LevelLabel" + (i + 1) + ".Text", String.valueOf(island.getLevel()));
                commandBuilder.set("#NameLabel" + (i + 1) + ".Text", island.getName());
                commandBuilder.set("#Island" + (i + 1) + ".Visible", true);
            } else {
                commandBuilder.set("#Island" + (i + 1) + ".Visible", false);
            }
        }
    }

    private void bindMenuEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "ClosePage"),
                false
        );

        for (int i = 1; i <= 9; i++) {
            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#Island" + i,
                    EventData.of("Action", "SelectIsland").append("IslandIndex", String.valueOf(i)),
                    false
            );
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PlayerIslandsPageData data) {
        System.out.println("Handling PlayerIslandsPageData event: " + data.action);

        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        final boolean refIsValid = playerRef != null && playerRef.isValid();

        switch (data.action) {
            case "SelectIsland" -> this.onSelectIsland(ref, store, playerRef, refIsValid, Integer.parseInt(data.islandIndex));
            case "CreateIsland" -> this.onCreateIsland(ref, store, playerRef, refIsValid);
            default -> this.onClose(ref, store);
        }
    }

    private void onSelectIsland(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid, int islandIndex) {
        if (!refIsValid) return;

        List<IslandData> islands = this.islandsManager.getIslandsForPlayer(playerRef.getUuid());

        if (islandIndex > 0 && islandIndex <= islands.size()) {
            IslandData selectedIsland = islands.get(islandIndex - 1);

            if(selectedIsland == null) return;

            Player player = store.getComponent(ref, Player.getComponentType());

            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, new IslandSettingsPage(playerRef, selectedIsland, this.islandsManager, this.islandLevelManager));
            }
        }
    }

    private void onCreateIsland(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (refIsValid) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    ColorUtil.colorize("&aCriando nova ilha...")
            );
        }

        // TODO: Create new island
        this.onClose(ref, store);
    }

    public void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;

        player.getPageManager().setPage(ref, store, Page.None);
    }
}
