package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class IslandInfoPageData {
    public static final BuilderCodec<IslandInfoPageData> CODEC = BuilderCodec.builder(IslandInfoPageData.class, IslandInfoPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .build();

    public String action;
}
