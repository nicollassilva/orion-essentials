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
import dev.thewarrior.SkyBlock.Managers.Levels.IslandLevelConfig;
import dev.thewarrior.SkyBlock.Pages.Data.IslandSettingsPageData;
import dev.thewarrior.SkyBlock.Pages.Utils.ConfirmDialog;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.List;
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
        // Nome e Título
        commandBuilder.set("#NameInput.Value", this.islandData.getName());
        commandBuilder.set("#TitleInput.Value", this.islandData.getEnterTitle() != null ? this.islandData.getEnterTitle() : "");

        // Nível
        int level = this.islandData.getLevel();
        commandBuilder.set("#LevelLabel.Text", "Nível " + level);

        // Progress Bar
        double currentXP = this.islandData.getExperience();
        List<IslandLevelConfig> levels = this.islandLevelManager.getData().getLevels();
        double nextXP = levels.stream().filter(l -> l.getLevel() == level + 1).findFirst().map(IslandLevelConfig::getRequiredPoints).orElse(currentXP);
        double progress = nextXP > 0 ? Math.min(currentXP / nextXP, 1.0) : 1.0;
        commandBuilder.set("#ProgressBar.Value", progress);
        commandBuilder.set("#XPNeededLabel.Text", "Faltam " + (int)(nextXP - currentXP) + " XP para o próximo nível");

        // Última Visita
        String lastVisit = "Nunca";
        if (this.islandData.getLastVisitData() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            lastVisit = sdf.format(this.islandData.getLastVisitData().getTime());
        }
        commandBuilder.set("#LastVisitLabel.Text", "Última visita: " + lastVisit);

        // Amigos (placeholder)
        commandBuilder.set("#FriendsPlaceholder.Text", "Amigos: " + this.islandData.getFriends().size());

        // Settings
        IslandSettings settings = this.islandData.getSettings();

        if(settings != null) {
            commandBuilder.set("#AllowVisitorsCheck #CheckBox.Value", settings.isAllowVisitors());
            commandBuilder.set("#AllowVisitorsChatCheck #CheckBox.Value", settings.isAllowVisitorsChat());
            commandBuilder.set("#AllowVisitorsBuildCheck #CheckBox.Value", settings.isAllowVisitorsToBuild());
            commandBuilder.set("#AllowFriendsVisitCheck #CheckBox.Value", settings.isAllowFriendsToVisit());
            commandBuilder.set("#AllowFriendsBuildCheck #CheckBox.Value", settings.isAllowFriendsToBuild());
            commandBuilder.set("#AllowFriendsDestroyCheck #CheckBox.Value", settings.isAllowFriendsToDestroy());
            commandBuilder.set("#PvpEnabledCheck #CheckBox.Value", settings.isPvpEnabled());
        }
    }

    private void bindMenuEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TeleportButton",
                EventData.of("Action", "TeleportToIsland"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#DeleteIslandButton",
                EventData.of("Action", "DeleteIsland"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveButton",
                EventData.of("Action", "SaveChanges")
                        .append("@NameInput", "#NameInput.Value")
                        .append("@TitleInput", "#TitleInput.Value")
                        .append("@AllowVisitorsCheck", "#AllowVisitorsCheck #CheckBox.Value")
                        .append("@AllowVisitorsChatCheck", "#AllowVisitorsChatCheck #CheckBox.Value")
                        .append("@AllowVisitorsBuildCheck", "#AllowVisitorsBuildCheck #CheckBox.Value")
                        .append("@AllowFriendsVisitCheck", "#AllowFriendsVisitCheck #CheckBox.Value")
                        .append("@AllowFriendsBuildCheck", "#AllowFriendsBuildCheck #CheckBox.Value")
                        .append("@AllowFriendsDestroyCheck", "#AllowFriendsDestroyCheck #CheckBox.Value")
                        .append("@PvpEnabledCheck", "#PvpEnabledCheck #CheckBox.Value")
                , false
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
            case "SaveChanges" -> this.onSaveChanges(ref, store, playerRef, refIsValid, data);
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
            default -> this.onClose(ref, store, true);
        }
    }

    private void onSaveChanges(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef, boolean refIsValid, IslandSettingsPageData data) {
        if (!refIsValid) return;

        boolean hasValidNameChanges = data.newName != null && !data.newName.isEmpty() && !data.newName.equals(this.islandData.getName());
        boolean hasValidTitleChanges = !data.newTitle.equals(this.islandData.getEnterTitle());

        if(hasValidNameChanges) this.islandData.setName(data.newName);
        if(hasValidTitleChanges) this.islandData.setEnterTitle(data.newTitle);

        boolean hasAllowVisitorsChange = this.islandData.getSettings().isAllowVisitors() != data.allowVisitors;
        boolean hasAllowVisitorsChatChange = this.islandData.getSettings().isAllowVisitorsChat() != data.allowVisitorsChat;
        boolean hasAllowVisitorsBuildChange = this.islandData.getSettings().isAllowVisitorsToBuild() != data.allowVisitorsBuild;
        boolean hasAllowFriendsVisitChange = this.islandData.getSettings().isAllowFriendsToVisit() != data.allowFriendsVisit;
        boolean hasAllowFriendsBuildChange = this.islandData.getSettings().isAllowFriendsToBuild() != data.allowFriendsBuild;
        boolean hasAllowFriendsDestroyChange = this.islandData.getSettings().isAllowFriendsToDestroy() != data.allowFriendsDestroy;
        boolean hasPvpEnabledChange = this.islandData.getSettings().isPvpEnabled() != data.pvpEnabled;

        boolean hasAnySettingsChanges = hasAllowVisitorsChange || hasAllowVisitorsChatChange || hasAllowVisitorsBuildChange ||
                hasAllowFriendsVisitChange || hasAllowFriendsBuildChange || hasAllowFriendsDestroyChange || hasPvpEnabledChange;

        if(hasAnySettingsChanges) {
            final IslandSettings settings = this.islandData.getSettings();

            if(hasAllowVisitorsChange) settings.setAllowVisitors(data.allowVisitors);
            if(hasAllowVisitorsChatChange) settings.setAllowVisitorsChat(data.allowVisitorsChat);
            if(hasAllowVisitorsBuildChange) settings.setAllowVisitorsToBuild(data.allowVisitorsBuild);
            if(hasAllowFriendsVisitChange) settings.setAllowFriendsToVisit(data.allowFriendsVisit);
            if(hasAllowFriendsBuildChange) settings.setAllowFriendsToBuild(data.allowFriendsBuild);
            if(hasAllowFriendsDestroyChange) settings.setAllowFriendsToDestroy(data.allowFriendsDestroy);
            if(hasPvpEnabledChange) settings.setPvpEnabled(data.pvpEnabled);
        }

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
