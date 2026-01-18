package dev.thewarrior.Pages.Permissions.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class PermissionsPageData {
    public static final BuilderCodec<PermissionsPageData> CODEC = BuilderCodec.builder(PermissionsPageData.class, PermissionsPageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .append(new KeyedCodec<>("@SearchValue", Codec.STRING),
                    (d, v) -> d.searchValue = v,
                    (d) -> d.searchValue).add()
            .append(new KeyedCodec<>("Target", Codec.STRING),
                    (d, v) -> d.target = v,
                    (d) -> d.target).add()
            .build();

    public String action;
    public String target;
    public String searchValue;
}
