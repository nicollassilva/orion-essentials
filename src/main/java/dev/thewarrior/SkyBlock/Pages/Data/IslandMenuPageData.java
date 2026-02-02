package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class IslandMenuPageData {
    public static final BuilderCodec<IslandMenuPageData> CODEC = BuilderCodec.builder(IslandMenuPageData.class, IslandMenuPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("Target", Codec.STRING),
                    (d, v) -> d.target = v,
                    (d) -> d.target).add()
            .build();

    public String action;
    public String target;
}

