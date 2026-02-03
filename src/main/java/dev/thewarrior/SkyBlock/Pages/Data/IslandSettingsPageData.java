package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class IslandSettingsPageData {
    public static final BuilderCodec<IslandSettingsPageData> CODEC = BuilderCodec.builder(IslandSettingsPageData.class, IslandSettingsPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("@NameInput", Codec.STRING),
                    (d, v) -> d.newName = v,
                    (d) -> d.newName).add()
            .append(new KeyedCodec<>("@TitleInput", Codec.STRING),
                    (d, v) -> d.newTitle = v,
                    (d) -> d.newTitle).add()
            .append(new KeyedCodec<>("@AllowVisitorsCheck", Codec.BOOLEAN),
                    (d, v) -> d.allowVisitors = v,
                    (d) -> d.allowVisitors).add()
            .append(new KeyedCodec<>("@AllowVisitorsChatCheck", Codec.BOOLEAN),
                    (d, v) -> d.allowVisitorsChat = v,
                    (d) -> d.allowVisitorsChat).add()
            .append(new KeyedCodec<>("@AllowVisitorsBuildCheck", Codec.BOOLEAN),
                    (d, v) -> d.allowVisitorsBuild = v,
                    (d) -> d.allowVisitorsBuild).add()
            .append(new KeyedCodec<>("@AllowFriendsVisitCheck", Codec.BOOLEAN),
                    (d, v) -> d.allowFriendsVisit = v,
                    (d) -> d.allowFriendsVisit).add()
            .append(new KeyedCodec<>("@AllowFriendsBuildCheck", Codec.BOOLEAN),
                    (d, v) -> d.allowFriendsBuild = v,
                    (d) -> d.allowFriendsBuild).add()
            .append(new KeyedCodec<>("@AllowFriendsDestroyCheck", Codec.BOOLEAN),
                    (d, v) -> d.allowFriendsDestroy = v,
                    (d) -> d.allowFriendsDestroy).add()
            .append(new KeyedCodec<>("@PvpEnabledCheck", Codec.BOOLEAN),
                    (d, v) -> d.pvpEnabled = v,
                    (d) -> d.pvpEnabled).add()
            .build();

    public String action;
    public String newName;
    public String newTitle;
    public boolean allowVisitors;
    public boolean allowVisitorsChat;
    public boolean allowVisitorsBuild;
    public boolean allowFriendsVisit;
    public boolean allowFriendsBuild;
    public boolean allowFriendsDestroy;
    public boolean pvpEnabled;
}
