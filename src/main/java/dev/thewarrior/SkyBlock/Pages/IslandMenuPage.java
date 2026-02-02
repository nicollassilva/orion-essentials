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
import dev.thewarrior.MiniGames.Utils.GameUtil;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Pages.Data.IslandMenuPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class IslandMenuPage extends InteractiveCustomUIPage<IslandMenuPageData> {
    private final IslandsManager islandsManager;
    private final PlayerRef playerRef;

    private IslandData currentIsland;

    public IslandMenuPage(@Nonnull PlayerRef playerRef, IslandsManager islandsManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandMenuPageData.CODEC);

        this.playerRef = playerRef;
        this.islandsManager = islandsManager;
    }

    @Override
    public void build(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl UICommandBuilder commandBuilder,
            @NonNullDecl UIEventBuilder eventBuilder,
            @NonNullDecl Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/SkyBlock/IslandMenuPage.ui");

//        this.loadCurrentIsland();
//
//        this.updateIslandInfo(commandBuilder);
//
        this.bindMenuEvents(eventBuilder);
    }

    private void loadCurrentIsland() {
        if (this.playerRef == null || !this.playerRef.isValid()) return;

        // Tentar encontrar a primeira ilha do jogador
        final var islands = this.islandsManager.getData().getIslandsForPlayer(this.playerRef);

        if (islands != null && !islands.isEmpty()) {
            this.currentIsland = islands.get(0);
        }
    }

    private void updateIslandInfo(UICommandBuilder commandBuilder) {
        if (this.currentIsland != null) {
            commandBuilder.set("#IslandNameLabel.Text", "Ilha: " + this.currentIsland.getIslandName());
            commandBuilder.set("#IslandLevelLabel.Text", "Nível " + this.currentIsland.getLevel());
        } else {
            commandBuilder.set("#IslandNameLabel.Text", "Nenhuma ilha selecionada");
            commandBuilder.set("#IslandLevelLabel.Text", "");
        }
    }

    private void bindMenuEvents(UIEventBuilder eventBuilder) {
        // Botão Ilha
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#IslandButton",
                EventData.of("Action", "OpenIsland"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SpawnButton",
                EventData.of("Action", "TeleportToSpawn"),
                false
        );

        // Botão Quests
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#QuestsButton",
                EventData.of("Action", "OpenQuests"),
                false
        );

        // Botão Market
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#FriendsButton",
                EventData.of("Action", "OpenFriends"),
                false
        );

        // Botão Skills
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#EconomyButton",
                EventData.of("Action", "OpenEconomy"),
                false
        );

        // Botão Perfil
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#RankingButton",
                EventData.of("Action", "OpenRanking"),
                false
        );

        // Botão Fechar
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "ClosePage"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandMenuPageData data) {
        System.out.println("Handling IslandMenuPageData event: " + data.action);

        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        final boolean refIsValid = playerRef != null && playerRef.isValid();

        switch (data.action) {
            case "TeleportToSpawn" -> this.onTeleportToSpawn(ref, store, playerRef, refIsValid);
            case "OpenIsland" -> this.onOpenIsland(ref, store, playerRef, refIsValid);
            case "OpenQuests" -> this.onOpenQuests(ref, store, playerRef, refIsValid);
            case "OpenFriends" -> this.onOpenFriends(ref, store, playerRef, refIsValid);
            case "OpenEconomy" -> this.onOpenEconomy(ref, store, playerRef, refIsValid);
            case "OpenRanking" -> this.onOpenRanking(ref, store, playerRef, refIsValid);
            default -> this.onClose(ref, store);
        }
    }

    private void onOpenIsland(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (refIsValid) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    ColorUtil.colorize("&aAbrindo configurações da ilha...")
            );
        }

        // TODO: Abrir página de configurações da ilha
        this.onClose(ref, store);
    }

    private void onOpenQuests(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (refIsValid) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    ColorUtil.colorize("&eAbrindo quests...")
            );
        }

        // TODO: Abrir página de quests
        this.onClose(ref, store);
    }

    private void onOpenEconomy(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (refIsValid) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    ColorUtil.colorize("&dAbrindo economia...")
            );
        }

        // TODO: Abrir página da economia
        this.onClose(ref, store);
    }

    private void onOpenRanking(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (refIsValid) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    ColorUtil.colorize("&cAbrindo ranking...")
            );
        }

        // TODO: Abrir página de ranking
        this.onClose(ref, store);
    }

    private void onTeleportToSpawn(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (!refIsValid) return;

        NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&bTeleporting to Spawn..."));
        GameUtil.teleportPlayerToServerSpawn(playerRef);

        this.onClose(ref, store);
    }

    private void onOpenFriends(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (refIsValid) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    ColorUtil.colorize("&6Abrindo amigos...")
            );
        }

        // TODO: Abrir página de amigos
        this.onClose(ref, store);
    }

    public void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;

        player.getPageManager().setPage(ref, store, Page.None);
    }
}

