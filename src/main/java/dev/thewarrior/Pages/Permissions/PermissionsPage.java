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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class PermissionsPage extends InteractiveCustomUIPage<PermissionsPageData> {
    private final Pattern permissionPattern = Pattern.compile("^[a-zA-Z0-9_.-]{1,64}$");

    private final PermissionManager permissionManager;
    private final Map<String, PermissionData> filteredGroups;

    private String selectedPermission = "";

    private String updatingPermission = "";
    private String updatedPermission = "";

    private boolean deleteConfirmed = false;

    public PermissionsPage(@Nonnull PlayerRef playerRef, PermissionManager permissionManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, PermissionsPageData.CODEC);

        this.filteredGroups = new Object2ObjectOpenHashMap<>();
        this.permissionManager = permissionManager;
    }

    public PermissionGroupsData getGroupsData() {
        return this.permissionManager.getGroupsData();
    }

    public int getGroupIndex() {
        int index = 0;

        Map<String, PermissionData> groupsToShow = this.getGroupsData().getGroups();

        if(!this.filteredGroups.isEmpty()) {
            groupsToShow = this.filteredGroups;
        }

        for (String key : groupsToShow.keySet()) {
            if(key.equalsIgnoreCase(this.selectedPermission)) {
                return index;
            }

            index++;
        }

        return -1;
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
        System.out.println("Handling PermissionsPageData event: " + data.action);

        switch (data.action) {
            // Permission cases
            case "AddPermission" -> this.onAdd(ref, store, data);
            case "SelectPermission" -> this.onSelect(ref, store, data.target, false);
            case "SearchPermission" -> this.onSearch(ref, store, data);
            case "UpdatePermissionName" -> {
                final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                final boolean refIsValid = playerRef != null && playerRef.isValid();
                boolean hasInitialErrors = false;

                if(this.permissionManager.isMandatoryPermission(this.selectedPermission)) {
                    if(refIsValid) NotificationUtil.sendNotification(
                            playerRef.getPacketHandler(), ColorUtil.colorize("&cNão é possível alterar o nome de um grupo de permissão obrigatório!")
                    );

                    hasInitialErrors = true;
                }

                if(!this.selectedPermission.startsWith("NewGroup_")) {
                    if(refIsValid) NotificationUtil.sendNotification(
                            playerRef.getPacketHandler(), ColorUtil.colorize("&cApenas grupos de permissão recém-criados podem ter seus nomes alterados!")
                    );

                    hasInitialErrors = true;
                }

                if(hasInitialErrors) {
                    this.sendUpdate(new UICommandBuilder().set("#PermissionInputName.Value", this.selectedPermission));
                    return;
                }

                final PermissionData permissionData = this.getGroupsData().getGroupData(this.selectedPermission);

                if(permissionData != null) {
                    permissionData.setUpdatedName(data.name);
                }
            }
            case "UpdatePermissionPrefix" -> {
                final PermissionData permissionData = this.getGroupsData().getGroupData(data.target);

                if (permissionData == null) return;

                permissionData.setPrefix(data.prefix, true);
            }
            case "UpdatePermissionSuffix" -> {
                final PermissionData permissionData = this.getGroupsData().getGroupData(data.target);

                if (permissionData == null) return;

                permissionData.setSuffix(data.suffix, true);
            }
            case "UpdatePermissionPriority" -> {
                final PermissionData permissionData = this.getGroupsData().getGroupData(data.target);

                if(permissionData == null) return;

                permissionData.setPriority(data.priority, true);
            }
            case "SavePermission" -> {
                final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                final boolean refIsValid = playerRef != null && playerRef.isValid();
                final PermissionData permissionData = this.getGroupsData().getGroupData(data.target);

                if(permissionData != null && permissionData.needsNameUpdate() && this.permissionManager.hasGroupData(permissionData.getUpdatedName())) {
                    if (refIsValid) NotificationUtil.sendNotification(
                            playerRef.getPacketHandler(), ColorUtil.colorize("&cJá existe um grupo de permissão com esse nome! Escolha outro nome para o grupo.")
                    );
                    return;
                }

                this.permissionManager.save(data.target);
                this.onClose(ref, store);

                if(refIsValid) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&2Permissão marcada para ser salva!"), data.target);
                }

                this.sendUpdate(new UICommandBuilder().set("#PermissionTitle.Text", ""));
            }
            case "DeletePermission" -> {
                final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                final boolean refIsValid = playerRef != null && playerRef.isValid();

                if(!this.deleteConfirmed) {
                    if(refIsValid) {
                        NotificationUtil.sendNotification(
                                playerRef.getPacketHandler(),
                                ColorUtil.colorize("&eClique novamente para confirmar a exclusão do grupo de permissão: &6" + this.selectedPermission)
                        );
                    }
                    this.deleteConfirmed = true;
                    return;
                }

                this.deleteConfirmed = false;
                this.permissionManager.deletePermission(this.selectedPermission);

                this.onClose(ref, store);

                if(refIsValid) {
                    NotificationUtil.sendNotification(
                            playerRef.getPacketHandler(),
                            ColorUtil.colorize("&2Grupo de permissão excluído com sucesso!"),
                            this.selectedPermission
                    );
                }
            }

            // Permission Node cases
            case "AddPermissionNode" -> {
                final PermissionData permissionData = this.getGroupsData().getGroupData(data.target);

                if(permissionData == null) return;

                permissionData.addPermission("new.permission" + System.currentTimeMillis());

                this.onSelect(ref, store, data.target, true);
            }
            case "OpenEditPermissionNode" -> this.onEditPermissionNode(ref, store, data);
            case "UpdatePermissionNode" -> this.updatedPermission = data.permissionNodeName;
            case "DetachPermissionNode" -> {
                final PermissionData permissionData = this.getGroupsData().getGroupData(this.selectedPermission);

                if(permissionData == null) return;

                permissionData.removePermission(data.target);

                this.onSelect(ref, store, this.selectedPermission, true);
            }
            default -> this.onClose(ref, store);
        }
    }

    private void onAdd(Ref<EntityStore> ref, Store<EntityStore> store, PermissionsPageData data) {
        if(this.getGroupsData() == null) return;

        final UIEventBuilder eventBuilder = new UIEventBuilder();
        final UICommandBuilder commandBuilder = new UICommandBuilder();
        final String randomName = "NewGroup_" + System.currentTimeMillis();

        this.getGroupsData().addGroup(randomName, getDefaultPermissions());

        this.sendPermissionsListUpdate(commandBuilder, eventBuilder, true);

        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if(playerRef == null) return;

        this.permissionManager.save(randomName);
        this.onSelect(ref, store, randomName, true);

        NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&2Grupo de permissão criado com sucesso!"), randomName);
    }

    @NonNullDecl
    private static List<String> getDefaultPermissions() {
        final List<String> defaultPermissions = new CopyOnWriteArrayList<>();

        defaultPermissions.add("multicommands.tpaon");
        defaultPermissions.add("multicommands.home");
        defaultPermissions.add("multicommands.tpaoff");
        defaultPermissions.add("multicommands.warp");
        defaultPermissions.add("multicommands.tell.*");
        defaultPermissions.add("multicommands.tpdeny");
        defaultPermissions.add("multicommands.tpa");
        defaultPermissions.add("multicommands.tpa");
        defaultPermissions.add("multicommands.tpaccept");
        defaultPermissions.add("multicommands.delhome");
        return defaultPermissions;
    }

    private void onSelect(Ref<EntityStore> ref, Store<EntityStore> store, String selectedPermission, boolean forceUpdate) {
        if(this.selectedPermission.equalsIgnoreCase(selectedPermission) && !forceUpdate) return;

        this.updatePermissionEntry(this.getGroupIndex(), "#2b3542");

        this.updatingPermission = "";
        this.selectedPermission = selectedPermission;
        this.deleteConfirmed = false;

        final UICommandBuilder commandBuilder = new UICommandBuilder();
        final UIEventBuilder eventBuilder = new UIEventBuilder();

        this.onSelectInternally(ref, store, commandBuilder, eventBuilder);

        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void onSelectInternally(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder commandBuilder, UIEventBuilder eventBuilder) {
        commandBuilder.clear("#PermissionName");
        commandBuilder.clear("#PermissionButtonActions");
        commandBuilder.clear("#PermissionFormActions");
        commandBuilder.clear("#PermissionContentList");

        if(this.selectedPermission == null || this.selectedPermission.isEmpty()) return;

        final PermissionData permissionData = this.getGroupsData().getGroupData(this.selectedPermission);

        if(permissionData == null) {
            final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

            if(playerRef == null || !playerRef.isValid()) return;

            NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cO grupo de permissão selecionado não foi encontrado!"));

            return;
        }

        this.updatePermissionEntry(this.getGroupIndex(), "#0a1119");

        commandBuilder.append("#PermissionButtonActions", "Pages/Permissions/PermissionHeaderActions.ui");
        commandBuilder.append("#PermissionFormActions", "Pages/Permissions/PermissionFormActions.ui");

        if(!this.selectedPermission.startsWith("NewGroup_")) {
            commandBuilder.append("#PermissionContentList", "Pages/Permissions/PermissionListHeaderActions.ui");

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#AddPermissionNode",
                    EventData.of("Action", "AddPermissionNode")
                            .append("Target", this.selectedPermission),
                    false
            );
        }

        //commandBuilder.set("#PermissionTitle.Text", permissionData.needsUpdate() ? "Possui modificações não salvas." : "");
        commandBuilder.set("#PermissionName.Text", this.selectedPermission);
        commandBuilder.set("#DeletePermission.Disabled", this.permissionManager.isMandatoryPermission(this.selectedPermission));
        commandBuilder.set("#PermissionInputName.Value", this.selectedPermission);
        commandBuilder.set("#PermissionInputPriority.Value", permissionData.getPriority());
        commandBuilder.set("#PermissionInputPrefix.Value", permissionData.getPrefix());
        commandBuilder.set("#PermissionInputSuffix.Value", permissionData.getSuffix());

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#DeletePermission",
                EventData.of("Action", "DeletePermission")
                        .append("Target", this.selectedPermission),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SavePermission",
                EventData.of("Action", "SavePermission")
                        .append("Target", this.selectedPermission),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#PermissionInputName",
                EventData.of("Action", "UpdatePermissionName")
                        .append("Target", this.selectedPermission)
                        .append("@PermissionName", "#PermissionInputName.Value"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#PermissionInputPriority",
                EventData.of("Action", "UpdatePermissionPriority")
                        .append("Target", this.selectedPermission)
                        .append("@PermissionPriority", "#PermissionInputPriority.Value"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#PermissionInputPrefix",
                EventData.of("Action", "UpdatePermissionPrefix")
                        .append("Target", this.selectedPermission)
                        .append("@PermissionPrefix", "#PermissionInputPrefix.Value"),
                false
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#PermissionInputSuffix",
                EventData.of("Action", "UpdatePermissionSuffix")
                        .append("Target", this.selectedPermission)
                        .append("@PermissionSuffix", "#PermissionInputSuffix.Value"),
                false
        );

        this.buildContentListContainer(commandBuilder, eventBuilder, permissionData);
    }

    public void updatePermissionEntry(int index, String background) {
        if(index == -1) return;

        final UICommandBuilder commandBuilder = new UICommandBuilder();

        commandBuilder.set("#Permissions[" + index + "].Background", background);

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

    private void onEditPermissionNode(Ref<EntityStore> ref, Store<EntityStore> store, PermissionsPageData data) {
        String[] parts = data.target.split("~", 2); // [selector, permissionNode]

        if(parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) return;

        final UICommandBuilder commandBuilder = new UICommandBuilder();
        final UIEventBuilder eventBuilder = new UIEventBuilder();

        final String selector = parts[0];
        final String permission = parts[1];

        if(this.updatingPermission.isEmpty()) {
            this.updatingPermission = permission;
            this.updatedPermission = permission;

            commandBuilder.set(selector + " #EditPermission #Label.Text", "Salvar");
            commandBuilder.set(selector + " #PermissionListEntryLabel.Visible", false);

            commandBuilder.set(selector + " #PermissionListEntryInputName.Visible", true);
            commandBuilder.set(selector + " #PermissionListEntryInputName.Value", permission);

            eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                    selector + " #PermissionListEntryInputName",
                    EventData.of("Action", "UpdatePermissionNode")
                            .append("Target", selector + "~" + permission)
                            .append("@PermissionNodeName", selector + " #PermissionListEntryInputName.Value"),
                    false
            );
        } else {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

            if(playerRef == null || !playerRef.isValid()) {
                playerRef = null;
            }

            if(!this.permissionPattern.matcher(this.updatedPermission).matches()) {
                if(playerRef != null) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cAlguma permissão possui caracteres inválidos! Use apenas letras, números, '_', '-' e '.' com no máximo 64 caracteres."));
                }

                return;
            }

            if(this.updatedPermission.isEmpty()) {
                if(playerRef != null) {
                    NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cO nome da permissão não pode estar vazio!"));
                }
            } else if(!this.updatingPermission.equals(this.updatedPermission)) {
                final PermissionData permissionData = this.getGroupsData().getGroupData(this.selectedPermission);

                if (permissionData != null) {
                    boolean isUpdated = permissionData.updatePermission(this.updatingPermission, this.updatedPermission);

                    if(!isUpdated && playerRef != null) {
                        NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&cNão foi possível encontrar a permissão para atualizar!"));
                    }
                }
            }

            this.updatingPermission = "";
            this.updatedPermission = "";

            commandBuilder.set(selector + " #EditPermission #Label.Text", "Editar");
            commandBuilder.set(selector + " #PermissionListEntryLabel.Visible", true);

            commandBuilder.set(selector + " #PermissionListEntryInputName.Visible", false);

            this.buildContentListContainer(commandBuilder, eventBuilder, this.getGroupsData().getGroupData(this.selectedPermission));
        }

        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void buildContentListContainer(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, PermissionData permissionData) {
        commandBuilder.clear("#PermissionContentListContainer");

        if(this.selectedPermission.startsWith("NewGroup_")) return;

        int count = 0;

        for (final String permissionNode : permissionData.getPermissions()) {
            String selector = "#PermissionContentListContainer[" + count + "]";

            commandBuilder.append("#PermissionContentListContainer", "Pages/Permissions/PermissionContentListEntry.ui");

            commandBuilder.set(selector + " #PermissionListEntryLabel.Text", permissionNode);

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #DetachPermission",
                    EventData.of("Action", "DetachPermissionNode").append("Target", permissionNode),
                    false
            );

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #EditPermission",
                    EventData.of("Action", "OpenEditPermissionNode").append("Target", selector + "~" + permissionNode),
                    false
            );

            count++;
        }
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
