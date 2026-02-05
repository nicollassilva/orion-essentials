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
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Pages.Data.IslandInvitePageData;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class IslandInvitePage extends InteractiveCustomUIPage<IslandInvitePageData> {
    private final IslandsManager islandsManager;
    private final PlayerRef playerRef;
    private final IslandData islandData;
    private final IslandLevelManager islandLevelManager;

    public IslandInvitePage(@Nonnull PlayerRef playerRef, IslandData islandData, IslandsManager islandsManager, IslandLevelManager islandLevelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandInvitePageData.CODEC);

        this.playerRef = playerRef;
        this.islandData = islandData;
        this.islandsManager = islandsManager;
        this.islandLevelManager = islandLevelManager;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/SkyBlock/IslandInvitePage.ui");

        // Bind invite by name button
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#InviteByNameButton",
                EventData.of("Action", "InviteByName").append("@Name", "#NameInput.Value"),
                false
        );

        // Bind close button
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "Close"),
                false
        );

        int entryIndex = 0;
        List<PlayerRef> onlinePlayers = Universe.get().getPlayers();

        for (final PlayerRef onlinePlayer : onlinePlayers) {
            if (this.islandData.isMember(onlinePlayer.getUuid())) continue;

            final Ref<EntityStore> onlinePlayerRef = onlinePlayer.getReference();

            if(onlinePlayerRef == null || !onlinePlayerRef.isValid()) continue;

            final Store<EntityStore> playerStore = onlinePlayerRef.getStore();
            final Player playerEntity = playerStore.getComponent(onlinePlayerRef, Player.getComponentType());

            if (playerEntity == null) continue;

            String entrySelector = "#PlayerList[" + entryIndex + "] ";

            commandBuilder.append("#PlayerList", "Pages/SkyBlock/IslandInviteEntry.ui");

            commandBuilder.set(entrySelector + "#PlayerNameLabel.Text", playerEntity.getDisplayName());

            // Bind invite button for this player
            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    entrySelector + "#InviteButton",
                    EventData.of("Action", "InvitePlayer").append("Uuid", onlinePlayer.getUuid().toString()),
                    false
            );

            entryIndex++;
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandInvitePageData data) {
        System.out.println("Handling IslandInvitePageData event: " + data.action);

        switch (data.action) {
            case "InviteByName" -> {
                if (data.name == null || data.name.isEmpty()) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cDigite um nome válido."));
                    return;
                }

                PlayerRef target = null;
                Player targetPlayer = null;

                final List<PlayerRef> onlinePlayers = Universe.get().getPlayers();

                for (PlayerRef p : onlinePlayers) {
                    if(this.islandData.isMember(p.getUuid())) continue;

                    final Ref<EntityStore> onlinePlayerRef = p.getReference();

                    if(onlinePlayerRef == null || !onlinePlayerRef.isValid()) continue;

                    final Store<EntityStore> playerStore = onlinePlayerRef.getStore();
                    final Player pPlayer = playerStore.getComponent(onlinePlayerRef, Player.getComponentType());

                    if (pPlayer != null && pPlayer.getDisplayName().equalsIgnoreCase(data.name)) {
                        target = p;
                        targetPlayer = pPlayer;
                        break;
                    }
                }

                if (target == null) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cJogador não encontrado ou offline."));
                    return;
                }

                if (this.islandData.isMember(target.getUuid())) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cEste jogador já é membro da ilha."));
                    return;
                }

                boolean added = this.islandData.addMember(target);
                this.islandsManager.save(islandData);

                NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&aO usuário &f" + targetPlayer.getDisplayName() + " &afoi adicionado nesta ilha."));

                reopenMembersPage(ref, store);
            }
            case "InvitePlayer" -> {
                if (data.uuid == null) return;

                final UUID targetUuid = UUID.fromString(data.uuid);
                final List<PlayerRef> onlinePlayers = Universe.get().getPlayers();

                PlayerRef target = null;
                Player targetPlayer = null;

                for (PlayerRef p : onlinePlayers) {
                    if(this.islandData.isMember(p.getUuid())) continue;

                    final Ref<EntityStore> onlinePlayerRef = p.getReference();

                    if(onlinePlayerRef == null || !onlinePlayerRef.isValid()) continue;

                    final Store<EntityStore> playerStore = onlinePlayerRef.getStore();

                    if (p.getUuid().equals(targetUuid)) {
                        target = p;
                        targetPlayer = playerStore.getComponent(onlinePlayerRef, Player.getComponentType());
                        break;
                    }
                }

                if (target == null || targetPlayer == null) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cJogador não encontrado."));
                    return;
                }

                if (islandData.isMember(target.getUuid())) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cEste jogador já é membro da ilha."));
                    return;
                }

                this.islandData.addMember(target);
                this.islandsManager.save(islandData);

                NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&aO usuário &f" + targetPlayer.getDisplayName() + "&a foi adicionado nesta ilha."));

                reopenMembersPage(ref, store);
            }
            case "Close" -> {
                reopenMembersPage(ref, store);
            }
        }
    }

    private void reopenMembersPage(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, new IslandMembersPage(playerRef, islandData, islandsManager, islandLevelManager));
        }
    }
}
