package dev.thewarrior.SkyBlock.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
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
import dev.thewarrior.Essentials.Managers.TeleportManager;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandSettings;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Pages.Data.IslandSettingsPageData;
import dev.thewarrior.SkyBlock.Pages.Utils.ConfirmDialog;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class IslandSettingsPage extends InteractiveCustomUIPage<IslandSettingsPageData> {
    private final IslandsManager islandsManager;
    private final IslandLevelManager islandLevelManager;
    private final PlayerRef playerRef;
    private final IslandData islandData;

    private volatile boolean isActive = true;

    public IslandSettingsPage(@Nonnull PlayerRef playerRef, IslandData islandData, IslandsManager islandsManager, IslandLevelManager islandLevelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandSettingsPageData.CODEC);

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
        commandBuilder.append("Pages/SkyBlock/IslandSettingsPage.ui");

        this.updateIslandInfo(commandBuilder);

        this.bindMenuEvents(eventBuilder);
    }

    private void updateIslandInfo(UICommandBuilder commandBuilder) {
        commandBuilder.set("#IslandNameLabel.Text", this.islandData.getName());
        commandBuilder.set("#IslandLevelLabel.Text", "Nível " + this.islandData.getLevel());

        // Assume owner name is available, e.g., this.islandData.getOwnerName()
        String ownerName = "Jogador"; // Placeholder
        commandBuilder.set("#OwnerLabel.Text", "Dono: " + ownerName);

        String lastVisit = "Nunca";
        if (this.islandData.getLastVisitData() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            lastVisit = sdf.format(this.islandData.getLastVisitData().getTime());
        }
        commandBuilder.set("#LastVisitLabel.Text", "Última visita: " + lastVisit);
    }

    private void bindMenuEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#InfoButton",
                EventData.of("Action", "OpenInfo"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#MembersButton",
                EventData.of("Action", "OpenMembers"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SettingsButton",
                EventData.of("Action", "OpenSettings"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#VisitsButton",
                EventData.of("Action", "OpenVisits"),
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
                "#DeleteButton",
                EventData.of("Action", "DeleteIsland"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TeleportButton",
                EventData.of("Action", "TeleportToIsland"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#StatsButton",
                EventData.of("Action", "OpenStats"),
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
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandSettingsPageData data) {
        System.out.println("Handling IslandSettingsPageData event: " + data.action);

        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        final boolean refIsValid = playerRef != null && playerRef.isValid();

        switch (data.action) {
            case "OpenInfo" -> {
                if (!refIsValid) return;

                Player player = store.getComponent(ref, Player.getComponentType());

                if (player != null) {
                    player.getPageManager().openCustomPage(ref, store, new IslandInfoPage(playerRef, islandData, islandsManager, islandLevelManager));
                }
            }
            case "OpenMembers" -> {
                if (!refIsValid) return;

                Player player = store.getComponent(ref, Player.getComponentType());

                if (player != null) {
                    player.getPageManager().openCustomPage(ref, store, new IslandMembersPage(playerRef, islandData, islandsManager, islandLevelManager));
                }
            }
            case "OpenSettings" -> {
                if (!refIsValid) return;

                Player player = store.getComponent(ref, Player.getComponentType());

                if (player != null) {
                    player.getPageManager().openCustomPage(ref, store, new IslandConfigPage(playerRef, this.islandData, this.islandsManager, this.islandLevelManager));
                }
            }
            case "OpenVisits" -> {
                if (refIsValid) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&eAbrindo visitas da ilha..."));
                }
                this.onClose(ref, store, false);
            }
            case "ExpandIsland" -> {
                if (refIsValid) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&eExpandindo ilha..."));
                }
                this.onClose(ref, store, false);
            }
            case "OpenStats" -> {
                if (refIsValid) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&eAbrindo estatísticas da ilha..."));
                }
                this.onClose(ref, store, false);
            }
            case "DeleteIsland" -> {
                if (!refIsValid) return;

                final Player player = store.getComponent(ref, Player.getComponentType());

                if(player == null || player.wasRemoved()) return;

                if(!this.playerRef.getUuid().equals(this.islandData.getOwnerId())) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cApenas o dono da ilha pode deletá-la."));
                    return;
                }

                ConfirmDialog dialog = new ConfirmDialog(playerRef,
                        ColorUtil.colorize("Confirme a remoção permanente da ilha: \n&l&e" + this.islandData.getName() + "&r&c\n\nESSA AÇÃO NÃO PODE SER DESFEITA."),
                        "Plant_Crop_Apple_Block",
                        () -> {
                            // TODO: Delete island logic
                            NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&aIlha deletada com sucesso."));

                            this.onClose(ref, store, true);
                        }, () -> player.getPageManager().openCustomPage(ref, store, new IslandSettingsPage(playerRef, islandData, islandsManager, islandLevelManager)));

                player.getPageManager().openCustomPage(ref, store, dialog);
            }
            case "TeleportToIsland" -> this.onTeleportToIsland(ref, store, playerRef, refIsValid);
            case "ClosePage" -> this.onClose(ref, store, true);
            default -> this.onClose(ref, store, false);
        }
    }

    private void onSaveChanges(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid, IslandSettingsPageData data) {
        if (!refIsValid) return;

        boolean hasValidNameChanges = data.newName != null && !data.newName.isEmpty() && !data.newName.equals(this.islandData.getName());
        boolean hasValidTitleChanges = !data.newTitle.equals(this.islandData.getEnterTitle());

        if(hasValidNameChanges) this.islandData.setName(data.newName);
        if(hasValidTitleChanges) this.islandData.setEnterTitle(data.newTitle);

        boolean hasAnySettingsChanges = this.checkAndUpdateIslandSettings(data);

        if(!hasValidNameChanges && !hasValidTitleChanges && !hasAnySettingsChanges) {
            NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&eNenhuma alteração foi feita nas configurações da ilha."));
            return;
        }

        this.islandData.setNeedsUpdate(true);
        this.islandsManager.save(this.islandData);

        final UICommandBuilder commandBuilder = new UICommandBuilder();

        commandBuilder.set("#SaveButton.Disabled", true);

        this.sendUpdate(commandBuilder);

        CompletableFuture.runAsync(() -> {
            if(!this.isActive) return;

            final UICommandBuilder enableButtonCommandBuilder = new UICommandBuilder();

            enableButtonCommandBuilder.set("#SaveButton.Disabled", false);

            this.sendUpdate(enableButtonCommandBuilder);
        }, CompletableFuture.delayedExecutor(3000, TimeUnit.MILLISECONDS));

        NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&aConfigurações da ilha salvas com sucesso!"));
    }

    private boolean checkAndUpdateIslandSettings(IslandSettingsPageData data) {
        boolean hasAllowVisitorsChange = this.islandData.getSettings().isAllowAnonymousToVisit() != data.allowVisitors;
        boolean hasAllowVisitorsChatChange = this.islandData.getSettings().isAllowAnonymousToChat() != data.allowVisitorsChat;
        boolean hasAllowVisitorsBuildChange = this.islandData.getSettings().isAllowVisitorsToBuild() != data.allowVisitorsBuild;
        boolean hasAllowFriendsVisitChange = this.islandData.getSettings().isAllowMembersToVisit() != data.allowFriendsVisit;
        boolean hasAllowFriendsBuildChange = this.islandData.getSettings().isAllowMembersToBuild() != data.allowFriendsBuild;
        boolean hasAllowFriendsDestroyChange = this.islandData.getSettings().isAllowMembersToDestroy() != data.allowFriendsDestroy;
        boolean hasPvpEnabledChange = this.islandData.getSettings().isPvpEnabled() != data.pvpEnabled;

        boolean hasAnySettingsChanges = hasAllowVisitorsChange || hasAllowVisitorsChatChange || hasAllowVisitorsBuildChange ||
                hasAllowFriendsVisitChange || hasAllowFriendsBuildChange || hasAllowFriendsDestroyChange || hasPvpEnabledChange;

        if(hasAnySettingsChanges) {
            final IslandSettings settings = this.islandData.getSettings();

            if(hasAllowVisitorsChange) settings.setAllowAnonymousToVisit(data.allowVisitors);
            if(hasAllowVisitorsChatChange) settings.setAllowAnonymousToChat(data.allowVisitorsChat);
            if(hasAllowVisitorsBuildChange) settings.setAllowVisitorsToBuild(data.allowVisitorsBuild);
            if(hasAllowFriendsVisitChange) settings.setAllowMembersToVisit(data.allowFriendsVisit);
            if(hasAllowFriendsBuildChange) settings.setAllowMembersToBuild(data.allowFriendsBuild);
            if(hasAllowFriendsDestroyChange) settings.setAllowMembersToDestroy(data.allowFriendsDestroy);
            if(hasPvpEnabledChange) settings.setPvpEnabled(data.pvpEnabled);
        }
        return hasAnySettingsChanges;
    }

    private void onTeleportToIsland(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid) {
        if (!refIsValid) return;

        Vector3d spawnLocation = this.islandData.getSpawnLocation();
        Vector3d spawnRotation = this.islandData.getSpawnRotation();

        TeleportManager.get().queueTeleport(
                playerRef, ref, store,
                playerRef.getTransform().getPosition(),
                this.islandData.getWorldName(),
                spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(),
                (float) spawnRotation.getY(), (float) spawnRotation.getX(),
                null
        );

        this.onClose(ref, store, false);
    }

    public void onClose(Ref<EntityStore> ref, Store<EntityStore> store, boolean reopenIslandPage) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;

        if(reopenIslandPage) {
            player.getPageManager().openCustomPage(ref, store, new PlayerIslandsPage(this.playerRef, this.islandsManager, this.islandLevelManager));
        } else {
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        this.isActive = false;
    }
}
