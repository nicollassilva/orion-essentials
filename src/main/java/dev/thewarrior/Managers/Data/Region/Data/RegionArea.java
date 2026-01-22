package dev.thewarrior.Managers.Data.Region.Data;

import com.hypixel.hytale.math.vector.Vector3d;
import dev.thewarrior.Managers.Data.Region.Composition.RegionType;

public class RegionArea {
    private Vector3d min;
    private Vector3d max;
    private Vector3d center;
    private int radius;
    private transient int radiusSquared;

    public void recompute() {
        this.radiusSquared = this.radius * this.radius;
    }

    public RegionArea() {}

    public boolean hasEmptyBounds() {
        return this.min == null && this.max == null && this.center == null;
    }

    public Vector3d getMin() {
        return this.min;
    }

    public void setMin(Vector3d min) {
        this.min = min;
    }

    public Vector3d getMax() {
        return this.max;
    }

    public void setMax(Vector3d max) {
        this.max = max;
    }

    public Vector3d getCenter() {
        return this.center;
    }

    public void setCenter(Vector3d center) {
        this.center = center;
    }

    public int getRadius() {
        return this.radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
    
    public int getRadiusSquared() {
        return this.radiusSquared;
    }

    public void setRadiusSquared(int radiusSquared) {
        this.radiusSquared = radiusSquared;
    }

    public String getMinString(final RegionType type) {
        if(this.min == null) return "N/A";

        return String.format("X: " + this.min.getX() + " | Y: " + (type.equals(RegionType.AREA) ? "~" : this.min.getY()) + " | Z: " + this.min.getZ());
    }

    public String getMaxString(final RegionType type) {
        if(this.max == null) return "N/A";

        return String.format("X: " + this.max.getX() + " | Y: " + (type.equals(RegionType.AREA) ? "~" : this.max.getY()) + " | Z: " + this.max.getZ());
    }

    public String getCenterString() {
        if(this.center == null) return "N/A";

        return String.format("X: %.0f, Y: %.0f, Z: %.0f", this.center.x, this.center.y, this.center.z);
    }
}
