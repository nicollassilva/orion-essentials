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
import com.hypixel.hytale.server.core.util.NotificationUtil;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Pages.Data.PlayerIslandsPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

        this.updateIslands(commandBuilder, eventBuilder);

        System.out.println("Olá");

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "ClosePage"),
                false
        );
    }

    private void updateIslands(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder) {
        List<IslandData> islands = this.islandsManager.getIslandsForPlayer(this.playerRef.getUuid());

        if(islands.isEmpty()) {
            commandBuilder.set("#EmptyState.Visible", true);
            commandBuilder.set("#Islands.Visible", false);

            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CreateIslandButton", EventData.of("Action", "CreateIsland"), false);
            return;
        }

        commandBuilder.set("#EmptyState.Visible", false);
        commandBuilder.set("#Islands.Visible", true);

        commandBuilder.clear("#Islands");

        for (int i = 0; i < islands.size(); i++) {
            IslandData island = islands.get(i);
            String selector = "#Islands[" + i + "]";

            commandBuilder.append("#Islands", "Pages/SkyBlock/IslandEntry.ui");

            commandBuilder.set(selector + " #LevelLabel.Text", String.valueOf(island.getLevel()));
            commandBuilder.set(selector + " #NameLabel.Text", island.getName());

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector,
                    EventData.of("Action", "SelectIsland").append("IslandIndex", String.valueOf(i + 1)),
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
        if (!refIsValid) return;

        this.islandsManager.createIslandForPlayerAsync(
                playerRef, store, ref,
                () -> playerRef.sendMessage(ColorUtil.colorize("&aIlha criada com sucesso! Teleportando você para sua nova ilha...")),
                () -> NotificationUtil.sendNotification(
                        playerRef.getPacketHandler(), ColorUtil.colorize("&cErro ao criar a ilha. Tente novamente mais tarde.")
                )
        ).thenAccept(_ -> CompletableFuture.runAsync(() -> {
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();

            this.updateIslands(commandBuilder, eventBuilder);
            this.sendUpdate(commandBuilder, eventBuilder, false);
        }, CompletableFuture.delayedExecutor(1500, TimeUnit.MILLISECONDS)));
    }

    public void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;

        player.getPageManager().openCustomPage(ref, store, new IslandMenuPage(this.playerRef, this.islandsManager, this.islandLevelManager));
    }
}
