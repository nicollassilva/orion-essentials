package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class IslandSettingsPageData {
    public static final BuilderCodec<IslandSettingsPageData> CODEC = BuilderCodec.builder(IslandSettingsPageData.class, IslandSettingsPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("@NewName", Codec.STRING),
                    (d, v) -> d.newName = v,
                    (d) -> d.newName).add()
            .append(new KeyedCodec<>("@NewTitle", Codec.STRING),
                    (d, v) -> d.newTitle = v,
                    (d) -> d.newTitle).add()
            .build();

    public String action;
    public String newName;
    public String newTitle;
}
