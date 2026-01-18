package dev.thewarrior.Components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Components.Data.PlayerHomes;
import dev.thewarrior.Utils.NamedLocation;

public class PlayerCommandComponent implements Component<EntityStore> {
    private boolean isTellOff = false;
    private boolean isTpaOff = false;
    private final PlayerHomes playerHomes = new PlayerHomes();

    public boolean isTellOff() {
        return isTellOff;
    }

    public void setTellOff(boolean tellOff) {
        isTellOff = tellOff;
    }

    public boolean isTpaOff() {
        return isTpaOff;
    }

    public void setTpaOff(boolean tpaOff) {
        isTpaOff = tpaOff;
    }

    public boolean hasHome(String name) {
        return playerHomes.hasHome(name);
    }

    public void addHome(String name, World world, Vector3d position, Vector3f rotation) {
        if(this.hasHome(name)) {
            this.removeHome(name);
        }

        playerHomes.addHome(new NamedLocation(
                name,
                world.getName(),
                position.x,
                position.y,
                position.z,
                rotation.x,
                rotation.y,
                rotation.z
        ));
    }

    public NamedLocation getHome(String name) {
        return playerHomes.getHome(name);
    }

    public NamedLocation[] getHomes() {
        return playerHomes.getHomes();
    }

    public int getHomesCount() {
        return this.getHomes().length;
    }

    public void removeHome(String name) {
        playerHomes.removeHome(name);
    }

    @Override
    public Component<EntityStore> clone() {
        final PlayerCommandComponent clone = new PlayerCommandComponent();
        clone.isTellOff = this.isTellOff;
        clone.isTpaOff = this.isTpaOff;
        clone.playerHomes.setHomes(this.playerHomes.getHomes());
        return clone;
    }

    public static final BuilderCodec<PlayerCommandComponent> CODEC = BuilderCodec.builder(PlayerCommandComponent.class, PlayerCommandComponent::new)
            .append(new KeyedCodec<>("TellOff", Codec.BOOLEAN),
                    (data, isOff) -> data.isTellOff = isOff,
                    (data) -> data.isTellOff).add()
            .append(new KeyedCodec<>("TpaOff", Codec.BOOLEAN),
                    (data, isOff) -> data.isTpaOff = isOff,
                    (data) -> data.isTpaOff).add()
            .append(new KeyedCodec<>("Homes", NamedLocation.ARRAY_CODEC),
                    (data, homes, _) -> data.playerHomes.setHomes(homes),
                    (data, _) -> data.playerHomes.getHomes()
            ).add()
            .build();

}
