package dev.thewarrior.Pages.Permissions;

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
import dev.thewarrior.Managers.Data.Permission.PermissionData;
import dev.thewarrior.Managers.Data.Permission.PermissionGroupsData;
import dev.thewarrior.Managers.PermissionManager;
import dev.thewarrior.Pages.Permissions.Data.PermissionsPageData;
import dev.thewarrior.Utils.ColorUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.Map;

public class PermissionsPage extends InteractiveCustomUIPage<PermissionsPageData> {
    private final PermissionManager permissionManager;
    private final Map<String, PermissionData> filteredGroups;

    private String selectedPermission = "";

    public PermissionsPage(@Nonnull PlayerRef playerRef, PermissionManager permissionManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, PermissionsPageData.CODEC);

        this.filteredGroups = new Object2ObjectOpenHashMap<>();
        this.permissionManager = permissionManager;
    }

    public PermissionGroupsData getGroupsData() {
        return this.permissionManager.getGroupsData();
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder commandBuilder, @NonNullDecl UIEventBuilder eventBuilder, @NonNullDecl Store<EntityStore> store) {
        commandBuilder.append("Pages/Permissions/PermissionsPage.ui");

        if(this.getGroupsData() == null) return;

        this.sendPermissionsListUpdate(commandBuilder, eventBuilder, false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#InsertButton", EventData.of("Action", "AddPermission"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("Action", "SearchPermission").append("@SearchValue", "#SearchInput.Value"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PermissionsPageData data) {
        switch (data.action) {
            case "AddPermission" -> this.onAdd(ref, store, data);
            case "SelectPermission" -> this.onSelect(ref, store, data);
            case "SearchPermission" -> this.onSearch(ref, store, data);
            default -> this.onClose(ref, store);
        }
    }

    private void onAdd(Ref<EntityStore> ref, Store<EntityStore> store, PermissionsPageData data) {
        if(this.getGroupsData() == null) return;

        final UIEventBuilder eventBuilder = new UIEventBuilder();
        final UICommandBuilder commandBuilder = new UICommandBuilder();
        final String randomName = "NewGroup_" + System.currentTimeMillis();

        this.getGroupsData().addGroup(randomName);

        this.sendPermissionsListUpdate(commandBuilder, eventBuilder, true);

        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if(playerRef == null) return;

        this.permissionManager.saveAsync(randomName).thenRun(() -> {
            NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&2Grupo de permissão criado com sucesso!"), randomName);
        }).exceptionally(ex -> {;
            NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cFalha ao criar o grupo de permissão: " + ex.getMessage()), randomName);
            return null;
        });
    }

    private void onSelect(Ref<EntityStore> ref, Store<EntityStore> store, PermissionsPageData data) {
        if(this.selectedPermission.equalsIgnoreCase(data.target)) return;

        this.selectedPermission = data.target;

        final UICommandBuilder commandBuilder = new UICommandBuilder();

        this.onSelectInternally(ref, store, commandBuilder);

        this.sendUpdate(commandBuilder);
    }

    private void onSearch(Ref<EntityStore> ref, Store<EntityStore> store, PermissionsPageData data) {
        final UIEventBuilder eventBuilder = new UIEventBuilder();
        final UICommandBuilder commandBuilder = new UICommandBuilder();

        if(data.searchValue.isEmpty()) {
            this.sendPermissionsListUpdate(commandBuilder, eventBuilder, true);
            return;
        }

        this.getGroupsData().getFilteredGroups(data.searchValue, this.filteredGroups::put);

        this.sendPermissionsListUpdate(commandBuilder, eventBuilder, true);
    }

    private void onSelectInternally(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder commandBuilder) {
        commandBuilder.clear("#PermissionName");
        commandBuilder.clear("#PermissionButtonActions");
        commandBuilder.clear("#PermissionFormActions");
        commandBuilder.clear("#PermissionContentList");
        commandBuilder.clear("#PermissionContentList");

        if(this.selectedPermission == null || this.selectedPermission.isEmpty()) return;

        final PermissionData permissionData = this.getGroupsData().getGroupData(this.selectedPermission);

        if(permissionData == null) {
            final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

            if(playerRef == null || !playerRef.isValid()) return;

            NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cO grupo de permissão selecionado não foi encontrado!"));

            return;
        }

        commandBuilder.append("#PermissionButtonActions", "Pages/Permissions/PermissionHeaderActions.ui");
        commandBuilder.append("#PermissionFormActions", "Pages/Permissions/PermissionFormActions.ui");
        commandBuilder.append("#PermissionContentList", "Pages/Permissions/PermissionContentList.ui");
        commandBuilder.append("#PermissionContentList", "Pages/Permissions/PermissionListHeaderActions.ui");

        commandBuilder.set("#PermissionName.Text", this.selectedPermission);
        commandBuilder.set("#PermissionInputName.Value", this.selectedPermission);
        commandBuilder.set("#PermissionInputPriority.Value", String.valueOf(permissionData.getPriority()));
        commandBuilder.set("#PermissionInputPrefix.Value", permissionData.getPrefix());
        commandBuilder.set("#PermissionInputSuffix.Value", permissionData.getSuffix());
    }

    private void sendPermissionsListUpdate(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, boolean sendUpdate) {
        int count = 0;

        Map<String, PermissionData> groupsToShow = this.getGroupsData().getGroups();

        if(!this.filteredGroups.isEmpty()) {
            groupsToShow = this.filteredGroups;
        }

        commandBuilder.clear("#Permissions");

        for (final String permission : groupsToShow.keySet()) {
            if(permission == null || permission.isEmpty()) continue;

            String selector = "#Permissions[" + count + "]";

            commandBuilder.append("#Permissions", "Pages/Permissions/PermissionEntry.ui");

            commandBuilder.set(selector + " #Name.Text", permission);

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector,
                    EventData.of("Action", "SelectPermission").append("Target", permission),
                    false
            );

            count++;
        }

        if(!sendUpdate) return;

        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    public void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if(player == null) return;

        player.getPageManager().setPage(ref, store, Page.None);
    }
}
