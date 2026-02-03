package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class IslandConfigPageData {
    public static final BuilderCodec<IslandConfigPageData> CODEC = BuilderCodec.builder(IslandConfigPageData.class, IslandConfigPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("@UpdatedName", Codec.STRING),
                    (d, v) -> d.updatedName = v,
                    (d) -> d.updatedName).add()
            .append(new KeyedCodec<>("@UpdatedTitle", Codec.STRING),
                    (d, v) -> d.updatedTitle = v,
                    (d) -> d.updatedTitle).add()
            .build();

    public String action = "";
    public String updatedName = "";
    public String updatedTitle = "";
}
