package dev.thewarrior.Essentials.Utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

public class NamedLocation {
    public static final BuilderCodec<NamedLocation> CODEC = BuilderCodec.builder(NamedLocation.class, NamedLocation::new)
            .append(new KeyedCodec<>("Name", Codec.STRING),
                    (p, v, extraInfo) -> p.name = v,
                    (p, extraInfo) -> p.name).add()
            .append(new KeyedCodec<>("World", Codec.STRING),
                    (p, v, extraInfo) -> p.world = v,
                    (p, extraInfo) -> p.world).add()
            .append(new KeyedCodec<>("X", Codec.DOUBLE),
                    (p, v, extraInfo) -> p.x = v,
                    (p, extraInfo) -> p.x).add()
            .append(new KeyedCodec<>("Y", Codec.DOUBLE),
                    (p, v, extraInfo) -> p.y = v,
                    (p, extraInfo) -> p.y).add()
            .append(new KeyedCodec<>("Z", Codec.DOUBLE),
                    (p, v, extraInfo) -> p.z = v,
                    (p, extraInfo) -> p.z).add()
            .append(new KeyedCodec<>("Yaw", Codec.FLOAT),
                    (p, v, extraInfo) -> p.yaw = v,
                    (p, extraInfo) -> p.yaw).add()
            .append(new KeyedCodec<>("Pitch", Codec.FLOAT),
                    (p, v, extraInfo) -> p.pitch = v,
                    (p, extraInfo) -> p.pitch).add()
            .append(new KeyedCodec<>("Roll", Codec.FLOAT),
                    (p, v, extraInfo) -> p.roll = v,
                    (p, extraInfo) -> p.roll).add()
            .build();

    public static final ArrayCodec<NamedLocation> ARRAY_CODEC = new ArrayCodec<>(CODEC, NamedLocation[]::new, NamedLocation::new);

    private String name;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private float roll;

    public NamedLocation() {}

    public NamedLocation(String name, String world, double x, double y, double z, float yaw, float pitch, float roll) {
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }
}
