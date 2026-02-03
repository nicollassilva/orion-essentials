package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class IslandMembersPageData {
    public static final BuilderCodec<IslandMembersPageData> CODEC = BuilderCodec.builder(IslandMembersPageData.class, IslandMembersPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("MemberIndex", Codec.STRING),
                    (d, v) -> d.memberIndex = v,
                    (d) -> d.memberIndex).add()
            .append(new KeyedCodec<>("@NewPermission", Codec.STRING),
                    (d, v) -> d.newPermission = v,
                    (d) -> d.newPermission).add()
            .build();

    public String action;
    public String memberIndex;
    public String newPermission;
}
