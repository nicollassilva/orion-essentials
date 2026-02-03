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
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandSettings;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Pages.Data.IslandConfigPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class IslandConfigPage extends InteractiveCustomUIPage<IslandConfigPageData> {
    private final PlayerRef playerRef;
    private final IslandData islandData;
    private final IslandSettings localSettings;

    private IslandsManager islandsManager;
    private IslandLevelManager islandLevelManager;

    public IslandConfigPage(@Nonnull PlayerRef playerRef, IslandData islandData, IslandsManager islandsManager, IslandLevelManager islandLevelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandConfigPageData.CODEC);

        this.playerRef = playerRef;
        this.islandData = islandData;
        this.islandsManager = islandsManager;
        this.islandLevelManager = islandLevelManager;

        this.localSettings = new IslandSettings();

        this.localSettings.setAllowVisitors(islandData.getSettings().isAllowVisitors());
        this.localSettings.setAllowVisitorsChat(islandData.getSettings().isAllowVisitorsChat());
        this.localSettings.setAllowVisitorsToBuild(islandData.getSettings().isAllowVisitorsToBuild());
        this.localSettings.setAllowFriendsToVisit(islandData.getSettings().isAllowFriendsToVisit());
        this.localSettings.setAllowFriendsToBuild(islandData.getSettings().isAllowFriendsToBuild());
        this.localSettings.setAllowFriendsToDestroy(islandData.getSettings().isAllowFriendsToDestroy());
        this.localSettings.setPvpEnabled(islandData.getSettings().isPvpEnabled());
    }

    @Override
    public void build(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl UICommandBuilder commandBuilder,
            @NonNullDecl UIEventBuilder eventBuilder,
            @NonNullDecl Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/SkyBlock/IslandConfigPage.ui");

        this.updateConfig(commandBuilder);

        this.bindMenuEvents(eventBuilder);
    }

    private void updateConfig(UICommandBuilder commandBuilder) {
        commandBuilder.set("#NameInput.Value", this.islandData.getName());
        commandBuilder.set("#TitleInput.Value", this.islandData.getEnterTitle() != null ? this.islandData.getEnterTitle() : "");

        // Allow Visitors
        this.updateButton(commandBuilder, "#AllowVisitorsButton", "#AllowVisitorsLabel", this.localSettings.isAllowVisitors());

        // Allow Visitors Chat
        this.updateButton(commandBuilder, "#AllowVisitorsChatButton", "#AllowVisitorsChatLabel", this.localSettings.isAllowVisitorsChat());

        // Allow Visitors Build
        this.updateButton(commandBuilder, "#AllowVisitorsBuildButton", "#AllowVisitorsBuildLabel", this.localSettings.isAllowVisitorsToBuild());

        // Allow Friends Visit
        this.updateButton(commandBuilder, "#AllowFriendsVisitButton", "#AllowFriendsVisitLabel", this.localSettings.isAllowFriendsToVisit());

        // Allow Friends Build
        this.updateButton(commandBuilder, "#AllowFriendsBuildButton", "#AllowFriendsBuildLabel", this.localSettings.isAllowFriendsToBuild());

        // Allow Friends Destroy
        this.updateButton(commandBuilder, "#AllowFriendsDestroyButton", "#AllowFriendsDestroyLabel", this.localSettings.isAllowFriendsToDestroy());

        // PvP Enabled
        this.updateButton(commandBuilder, "#PvpEnabledButton", "#PvpEnabledLabel", this.localSettings.isPvpEnabled());
    }

    private void updateButton(UICommandBuilder commandBuilder, String buttonId, String labelId, boolean value) {
        if (value) {
            commandBuilder.set(buttonId + ".Background", "#497B51");
            commandBuilder.set(buttonId + ".Text", "ON");
        } else {
            commandBuilder.set(buttonId + ".Background", "#FA2E12");
            commandBuilder.set(buttonId + ".Text", "OFF");
        }
    }

    private void bindMenuEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AllowVisitorsButton",
                EventData.of("Action", "ToggleAllowVisitors"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AllowVisitorsChatButton",
                EventData.of("Action", "ToggleAllowVisitorsChat"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AllowVisitorsBuildButton",
                EventData.of("Action", "ToggleAllowVisitorsBuild"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AllowFriendsVisitButton",
                EventData.of("Action", "ToggleAllowFriendsVisit"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AllowFriendsBuildButton",
                EventData.of("Action", "ToggleAllowFriendsBuild"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AllowFriendsDestroyButton",
                EventData.of("Action", "ToggleAllowFriendsDestroy"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#PvpEnabledButton",
                EventData.of("Action", "TogglePvpEnabled"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveButton",
                EventData.of("Action", "Save").append("@UpdatedName", "#NameInput.Value").append("@UpdatedTitle", "#TitleInput.Value"),
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
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandConfigPageData data) {
        System.out.println("Handling IslandConfigPageData event: " + data.action);

        switch (data.action) {
            case "ToggleAllowVisitors" -> {
                this.localSettings.setAllowVisitors(!this.localSettings.isAllowVisitors());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "ToggleAllowVisitorsChat" -> {
                this.localSettings.setAllowVisitorsChat(!this.localSettings.isAllowVisitorsChat());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "ToggleAllowVisitorsBuild" -> {
                this.localSettings.setAllowVisitorsToBuild(!this.localSettings.isAllowVisitorsToBuild());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "ToggleAllowFriendsVisit" -> {
                this.localSettings.setAllowFriendsToVisit(!this.localSettings.isAllowFriendsToVisit());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "ToggleAllowFriendsBuild" -> {
                this.localSettings.setAllowFriendsToBuild(!this.localSettings.isAllowFriendsToBuild());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "ToggleAllowFriendsDestroy" -> {
                this.localSettings.setAllowFriendsToDestroy(!this.localSettings.isAllowFriendsToDestroy());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "TogglePvpEnabled" -> {
                this.localSettings.setPvpEnabled(!this.localSettings.isPvpEnabled());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "Save" -> this.onSave(ref, store, data);
            case "ClosePage" -> this.onClose(ref, store);
            default -> this.onClose(ref, store);
        }
    }

    private void onSave(Ref<EntityStore> ref, Store<EntityStore> store, IslandConfigPageData data) {
        if(!data.updatedName.isBlank() && !this.islandData.getName().equals(data.updatedName)) {
            this.islandData.setName(data.updatedName);
        }

        if(!this.islandData.getEnterTitle().equals(data.updatedTitle)) {
            this.islandData.setEnterTitle(data.updatedTitle);
        }

        this.islandData.getSettings().setAllowVisitors(this.localSettings.isAllowVisitors());
        this.islandData.getSettings().setAllowVisitorsChat(this.localSettings.isAllowVisitorsChat());
        this.islandData.getSettings().setAllowVisitorsToBuild(this.localSettings.isAllowVisitorsToBuild());
        this.islandData.getSettings().setAllowFriendsToVisit(this.localSettings.isAllowFriendsToVisit());
        this.islandData.getSettings().setAllowFriendsToBuild(this.localSettings.isAllowFriendsToBuild());
        this.islandData.getSettings().setAllowFriendsToDestroy(this.localSettings.isAllowFriendsToDestroy());
        this.islandData.getSettings().setPvpEnabled(this.localSettings.isPvpEnabled());

        this.islandData.setNeedsUpdate(true);

        this.islandsManager.save(this.islandData);

        this.onClose(ref, store);
    }

    private void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, new IslandSettingsPage(this.playerRef, this.islandData, this.islandsManager, this.islandLevelManager)); // Assuming managers are null for now
        }
    }
}
