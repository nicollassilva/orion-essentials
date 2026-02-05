package dev.thewarrior.SkyBlock.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
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

    private final IslandsManager islandsManager;
    private final IslandLevelManager islandLevelManager;

    public IslandConfigPage(@Nonnull PlayerRef playerRef, IslandData islandData, IslandsManager islandsManager, IslandLevelManager islandLevelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandConfigPageData.CODEC);

        this.playerRef = playerRef;
        this.islandData = islandData;
        this.islandsManager = islandsManager;
        this.islandLevelManager = islandLevelManager;

        this.localSettings = islandData.getSettings().copy();
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

        this.updateButton(commandBuilder, "#AllowVisitorsButton", this.localSettings.isAllowAnonymousToVisit());
        this.updateButton(commandBuilder, "#AllowVisitorsChatButton", this.localSettings.isAllowAnonymousToChat());
        this.updateButton(commandBuilder, "#AllowVisitorsBuildButton", this.localSettings.isAllowVisitorsToBuild());
        this.updateButton(commandBuilder, "#AllowMembersVisitButton", this.localSettings.isAllowMembersToVisit());
        this.updateButton(commandBuilder, "#AllowMembersBuildButton", this.localSettings.isAllowMembersToBuild());
        this.updateButton(commandBuilder, "#AllowMembersDestroyButton", this.localSettings.isAllowMembersToDestroy());
        this.updateButton(commandBuilder, "#PvpEnabledButton", this.localSettings.isPvpEnabled());
    }

    private void updateButton(UICommandBuilder commandBuilder, String buttonId, boolean value) {
        Anchor anchor = new Anchor();

        anchor.setWidth(Value.of(17));
        anchor.setHeight(Value.of(25));

        if (value) {
            anchor.setLeft(Value.of(26));

            commandBuilder.set(buttonId.replace("Button", "Group") + ".Background", "#74a77c");
            commandBuilder.set(buttonId + ".Background", "#497B51");
            commandBuilder.setObject(buttonId + " #Handler.Anchor", anchor);
        } else {
            anchor.setLeft(Value.of(-11));

            commandBuilder.set(buttonId.replace("Button", "Group") + ".Background", "#FA2E12");
            commandBuilder.set(buttonId + ".Background", "#C12E12");
            commandBuilder.setObject(buttonId + " #Handler.Anchor", anchor);
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
                "#AllowMembersVisitButton",
                EventData.of("Action", "ToggleAllowMembersVisit"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AllowMembersBuildButton",
                EventData.of("Action", "ToggleAllowMembersBuild"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AllowMembersDestroyButton",
                EventData.of("Action", "ToggleAllowMembersDestroy"),
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
                this.localSettings.setAllowAnonymousToVisit(!this.localSettings.isAllowAnonymousToVisit());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "ToggleAllowVisitorsChat" -> {
                this.localSettings.setAllowAnonymousToChat(!this.localSettings.isAllowAnonymousToChat());
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
            case "ToggleAllowMembersVisit" -> {
                this.localSettings.setAllowMembersToVisit(!this.localSettings.isAllowMembersToVisit());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "ToggleAllowMembersBuild" -> {
                this.localSettings.setAllowMembersToBuild(!this.localSettings.isAllowMembersToBuild());
                UICommandBuilder commandBuilder = new UICommandBuilder();
                this.updateConfig(commandBuilder);
                this.sendUpdate(commandBuilder);
            }
            case "ToggleAllowMembersDestroy" -> {
                this.localSettings.setAllowMembersToDestroy(!this.localSettings.isAllowMembersToDestroy());
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

        this.islandData.getSettings().setAllowAnonymousToVisit(this.localSettings.isAllowAnonymousToVisit());
        this.islandData.getSettings().setAllowAnonymousToChat(this.localSettings.isAllowAnonymousToChat());
        this.islandData.getSettings().setAllowVisitorsToBuild(this.localSettings.isAllowVisitorsToBuild());
        this.islandData.getSettings().setAllowMembersToVisit(this.localSettings.isAllowMembersToVisit());
        this.islandData.getSettings().setAllowMembersToBuild(this.localSettings.isAllowMembersToBuild());
        this.islandData.getSettings().setAllowMembersToDestroy(this.localSettings.isAllowMembersToDestroy());
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
