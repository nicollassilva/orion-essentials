package dev.thewarrior.SkyBlock.Pages.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class PlayerIslandsPageData {
    public static final BuilderCodec<PlayerIslandsPageData> CODEC = BuilderCodec.builder(PlayerIslandsPageData.class, PlayerIslandsPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("IslandIndex", Codec.STRING),
                    (d, v) -> d.islandIndex = v,
                    (d) -> d.islandIndex).add()
            .build();

    public String action;
    public String islandIndex; // 1-9 for the island selected
}
