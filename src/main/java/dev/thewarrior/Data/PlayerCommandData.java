package dev.thewarrior.Data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerCommandData implements Component<EntityStore> {
    private boolean isTellOff = false;

    public boolean isTellOff() {
        return isTellOff;
    }

    public void setTellOff(boolean tellOff) {
        isTellOff = tellOff;
    }

    @Override
    public Component<EntityStore> clone() {
        final PlayerCommandData clone = new PlayerCommandData();
        clone.isTellOff = this.isTellOff;
        return clone;
    }

    public static final BuilderCodec<PlayerCommandData> CODEC = BuilderCodec.builder(PlayerCommandData.class, PlayerCommandData::new)
            .append(
                    new KeyedCodec<>("TellOff", Codec.BOOLEAN),
                    (data, isOff) -> data.isTellOff = isOff,
                    (data) -> data.isTellOff
            )
            .add()
            .build();
}
