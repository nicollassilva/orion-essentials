package dev.thewarrior.Pages.Permissions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Pages.Permissions.Data.PermissionsPageData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionsPage extends InteractiveCustomUIPage<PermissionsPageData> {
    private final PermissionProvider permissionProvider;
    private Set<String> permissions;
    private String selectedPermission = "";

    public PermissionsPage(@Nonnull PlayerRef playerRef, List<PermissionProvider> permissions) {
        super(playerRef, CustomPageLifetime.CanDismiss, PermissionsPageData.CODEC);

        this.permissionProvider = permissions.getFirst();

        this.permissions = this.permissionProvider.getGroupsForUser(this.playerRef.getUuid());
    }

    @Override
    public void build(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl UICommandBuilder commandBuilder,
            @NonNullDecl UIEventBuilder eventBuilder,
            @NonNullDecl Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/Permissions/PermissionsPage.ui");

        if(this.permissions == null || this.permissions.isEmpty()) return;

        this.sendPermissionsListUpdate(commandBuilder, eventBuilder, this.permissions, false);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#InsertButton", EventData.of("Action", "AddPermission"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("Action", "SearchPermission").append("@SearchValue", "#SearchInput.Value"), false);
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull PermissionsPageData data
    ) {
        switch (data.action) {
            case "AddPermission" -> {
                System.out.println("Add Permission button clicked");
            }
            case "SelectPermission" -> {
                if(this.selectedPermission.equalsIgnoreCase(data.target)) return;

                this.selectedPermission = data.target;

                final UICommandBuilder commandBuilder = new UICommandBuilder();

                commandBuilder.clear("#PermissionName");
                commandBuilder.set("#PermissionName.Text", this.selectedPermission);

                commandBuilder.clear("#PermissionButtonActions");
                commandBuilder.append("#PermissionButtonActions", "Pages/Permissions/PermissionHeaderActions.ui");

                commandBuilder.clear("#PermissionFormActions");
                commandBuilder.append("#PermissionFormActions", "Pages/Permissions/PermissionFormActions.ui");

                commandBuilder.clear("#PermissionInputName.Value");
                commandBuilder.set("#PermissionInputName.Value", this.selectedPermission);

                commandBuilder.clear("#PermissionContentList");
                commandBuilder.append("#PermissionContentList", "Pages/Permissions/PermissionContentList.ui");

                commandBuilder.clear("#PermissionContentList");
                commandBuilder.append("#PermissionContentList", "Pages/Permissions/PermissionListHeaderActions.ui");

                this.sendUpdate(commandBuilder);
            }
            case "SearchPermission" -> {
                final UIEventBuilder eventBuilder = new UIEventBuilder();
                final UICommandBuilder commandBuilder = new UICommandBuilder();

                if(data.searchValue.isEmpty()) {
                    this.sendPermissionsListUpdate(commandBuilder, eventBuilder, this.permissions, true);
                    return;
                }

                final Set<String> permissions = this.permissions.stream().filter(
                        permission -> permission.toLowerCase().contains(data.searchValue.toLowerCase())
                ).collect(Collectors.toSet());

                this.sendPermissionsListUpdate(commandBuilder, eventBuilder, permissions, true);
            }
            default -> closePage(ref, store);
        }
    }

    private void sendPermissionsListUpdate(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Set<String> permissions, boolean sendUpdate) {
        int count = 0;

        commandBuilder.clear("#Permissions");

        for (String permission : permissions) {
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

    public void closePage(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if(player == null) return;

        player.getPageManager().setPage(ref, store, Page.None);
    }
}
