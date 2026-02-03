package dev.thewarrior.SkyBlock.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.Islands.Data.IslandFriendData;
import dev.thewarrior.SkyBlock.Managers.Islands.Enum.IslandFriendPermission;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Pages.Data.IslandMembersPageData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;

public class IslandMembersPage extends InteractiveCustomUIPage<IslandMembersPageData> {
    private final IslandsManager islandsManager;
    private final PlayerRef playerRef;
    private final IslandData islandData;
    private final IslandLevelManager islandLevelManager;

    public IslandMembersPage(@Nonnull PlayerRef playerRef, IslandData islandData, IslandsManager islandsManager, IslandLevelManager islandLevelManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandMembersPageData.CODEC);

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
        commandBuilder.append("Pages/SkyBlock/IslandMembersPage.ui");

        ObjectArrayList<IslandFriendData> friends = this.islandData.getFriends();
        int memberCount = friends.size();

        commandBuilder.set("#TitleLabel.Text", "Membros da Ilha (" + memberCount + "/5)");

        // Bind invite button
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#InviteButton",
                EventData.of("Action", "Invite"),
                false
        );

        // Bind close button
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "Close"),
                false
        );

        // Dynamically add member entries
        for (int i = 0; i < friends.size(); i++) {
            IslandFriendData friend = friends.get(i);
            String entrySelector = "#MemberList[" + i + "] ";

            commandBuilder.append("#MemberList", "Pages/SkyBlock/IslandMemberEntry.ui");

            // Set dropdown entries
            ObjectArrayList<DropdownEntryInfo> permissionEntries = new ObjectArrayList<>();

            permissionEntries.add(new DropdownEntryInfo(LocalizableString.fromString("Default"), "default"));
            permissionEntries.add(new DropdownEntryInfo(LocalizableString.fromString("Builder"), "build"));
            permissionEntries.add(new DropdownEntryInfo(LocalizableString.fromString("Administrador"), "co_owner"));

            commandBuilder.set(entrySelector + "#PermissionDropdown.Entries", permissionEntries);

            commandBuilder.set(entrySelector + "#MemberNameLabel.Text", friend.getNickname());

            // Set current value
            commandBuilder.set(entrySelector + "#PermissionDropdown.Value", friend.getPermission().getPermissionName());

            // Bind permission change
            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.ValueChanged,
                    entrySelector + "#PermissionDropdown",
                    EventData.of("Action", "ChangePermission").append("MemberIndex", String.valueOf(i)).append("@NewPermission", entrySelector + "#PermissionDropdown.Value"),
                    false
            );

            // Bind remove button
            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    entrySelector + "#RemoveButton",
                    EventData.of("Action", "RemoveMember").append("MemberIndex", String.valueOf(i)),
                    false
            );
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandMembersPageData data) {
        System.out.println("Handling IslandMembersPageData event: " + data.action);

        int memberIndex = -1;

        try {
            memberIndex = Integer.parseInt(data.memberIndex);
        } catch (NumberFormatException ignored) {}

        switch (data.action) {
            case "Invite" -> {
                if(this.playerRef == null || !this.playerRef.isValid()) return;

                Player player = store.getComponent(ref, Player.getComponentType());

                if (player != null) {
                    player.getPageManager().openCustomPage(ref, store, new IslandInvitePage(playerRef, islandData, islandsManager, islandLevelManager));
                }
            }
            case "ChangePermission" -> {
                if (memberIndex < 0 || memberIndex >= islandData.getFriends().size() || data.newPermission == null || data.newPermission.isBlank()) return;

                IslandFriendData friend = islandData.getFriends().get(memberIndex);
                IslandFriendPermission newPerm = IslandFriendPermission.valueOf(data.newPermission.toUpperCase());

                friend.setPermission(newPerm);

                islandData.setNeedsUpdate(true);
                islandsManager.save(islandData);

                NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&aPermissão de &f" + friend.getNickname() + "&a alterada para &e" + newPerm.getPermissionName() + "&a."));
            }
            case "RemoveMember" -> {
                if (memberIndex < 0 || memberIndex >= islandData.getFriends().size()) return;

                IslandFriendData friend = islandData.getFriends().get(memberIndex);

                islandData.removeFriend(friend.getUuid());
                islandsManager.save(islandData);

                NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&aO usuário &f" + friend.getNickname() + "&a foi removido da sua ilha."));

                Player player = store.getComponent(ref, Player.getComponentType());

                if (player != null) {
                    player.getPageManager().openCustomPage(ref, store, new IslandMembersPage(playerRef, islandData, islandsManager, islandLevelManager));
                }
            }
            case "Close" -> {
                this.onClose(ref, store);
            }
        }
    }

    private void onClose(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, new IslandSettingsPage(this.playerRef, this.islandData, this.islandsManager, this.islandLevelManager));
        }
    }
}
