package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class IslandInvitePageData {
    public static final BuilderCodec<IslandInvitePageData> CODEC = BuilderCodec.builder(IslandInvitePageData.class, IslandInvitePageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("@Name", Codec.STRING),
                    (d, v) -> d.name = v,
                    (d) -> d.name).add()
            .append(new KeyedCodec<>("Uuid", Codec.STRING),
                    (d, v) -> d.uuid = v,
                    (d) -> d.uuid).add()
            .build();

    public String action;
    public String name;
    public String uuid;
}
