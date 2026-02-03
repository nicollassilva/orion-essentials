package dev.thewarrior.SkyBlock.Pages.Utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class ConfirmDialogData {
    public static final BuilderCodec<ConfirmDialogData> CODEC = BuilderCodec.builder(ConfirmDialogData.class, ConfirmDialogData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING),
                    (d, v) -> d.action = v,
                    (d) -> d.action).add()
            .build();

    public String action;
}
