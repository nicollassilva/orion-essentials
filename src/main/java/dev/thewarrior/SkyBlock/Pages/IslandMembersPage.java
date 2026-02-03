package dev.thewarrior.SkyBlock.Pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import dev.thewarrior.Essentials.Utils.ColorUtil;
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

    private volatile boolean isActive = true;

    public IslandMembersPage(@Nonnull PlayerRef playerRef, IslandData islandData, IslandsManager islandsManager) {
        super(playerRef, CustomPageLifetime.CanDismiss, IslandMembersPageData.CODEC);

        this.playerRef = playerRef;
        this.islandData = islandData;
        this.islandsManager = islandsManager;
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

            permissionEntries.add(new DropdownEntryInfo(LocalizableString.fromString("Padrão"), "default"));
            permissionEntries.add(new DropdownEntryInfo(LocalizableString.fromString("Construir"), "build"));
            permissionEntries.add(new DropdownEntryInfo(LocalizableString.fromString("Co-Dono"), "co_owner"));

            commandBuilder.set(entrySelector + "#PermissionDropdown.Entries", permissionEntries);

            commandBuilder.set(entrySelector + "#MemberNameLabel.Text", friend.getNickname());

            // Set current value
            commandBuilder.set(entrySelector + "#PermissionDropdown.Value", friend.getPermission().getPermissionName());

            // Bind permission change
            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.ValueChanged,
                    entrySelector + "#PermissionDropdown",
                    EventData.of("Action", "ChangePermission").append("MemberIndex", String.valueOf(i + 1)),
                    false
            );

            // Bind remove button
            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    entrySelector + "#RemoveButton",
                    EventData.of("Action", "RemoveMember").append("MemberIndex", String.valueOf(i + 1)),
                    false
            );
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IslandMembersPageData data) {
        System.out.println("Handling IslandMembersPageData event: " + data.action);

        switch (data.action) {
            case "Invite" -> {
                // TODO: Implement invite logic, perhaps open a page to select player
                NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&eConvite enviado..."));
            }
            case "ChangePermission" -> {
                if (data.memberIndex < 0 || data.memberIndex >= islandData.getFriends().size()) return;
                IslandFriendData friend = islandData.getFriends().get(data.memberIndex);
                IslandFriendPermission newPerm = IslandFriendPermission.valueOf(data.newPermission.toUpperCase());
                friend.setPermission(newPerm);
                islandData.setNeedsUpdate(true);
                islandsManager.save(islandData);
                NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&aPermissão alterada."));
            }
            case "RemoveMember" -> {
                if (data.memberIndex < 0 || data.memberIndex >= islandData.getFriends().size()) return;
                IslandFriendData friend = islandData.getFriends().get(data.memberIndex);
                islandData.removeFriend(friend.getUuid());
                islandsManager.save(islandData);
                NotificationUtil.sendNotification(playerRef.getPacketHandler(), ColorUtil.colorize("&aMembro removido."));
                // Reopen the page
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    player.getPageManager().openCustomPage(ref, store, new IslandMembersPage(playerRef, islandData, islandsManager));
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
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        this.isActive = false;
    }
}
