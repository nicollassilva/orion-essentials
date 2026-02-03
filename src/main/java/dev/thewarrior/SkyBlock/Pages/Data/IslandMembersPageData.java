package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class IslandMembersPageData {
    public static final BuilderCodec<IslandMembersPageData> CODEC = BuilderCodec.builder(IslandMembersPageData.class, IslandMembersPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("MemberIndex", Codec.INTEGER),
                    (d, v) -> d.memberIndex = v,
                    (d) -> d.memberIndex).add()
            .append(new KeyedCodec<>("NewPermission", Codec.STRING),
                    (d, v) -> d.newPermission = v,
                    (d) -> d.newPermission).add()
            .build();

    public String action;
    public int memberIndex;
    public String newPermission;

    public static IslandMembersPageData of(String action) {
        IslandMembersPageData data = new IslandMembersPageData();
        data.action = action;
        return data;
    }

    public static IslandMembersPageData of(String action, int memberIndex, String newPermission) {
        IslandMembersPageData data = new IslandMembersPageData();
        data.action = action;
        data.memberIndex = memberIndex;
        data.newPermission = newPermission;
        return data;
    }
}
