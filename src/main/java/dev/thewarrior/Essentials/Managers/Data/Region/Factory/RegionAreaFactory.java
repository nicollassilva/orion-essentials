package dev.thewarrior.Essentials.Managers.Data.Region.Factory;

import com.hypixel.hytale.math.vector.Vector3d;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionArea;

public class RegionAreaFactory {
    public RegionArea createGlobal() {
        return new RegionArea();
    }

    public RegionArea createArea(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        RegionArea region = new RegionArea();

        region.setMin(new Vector3d(Math.min(minX, maxX), minY, Math.min(minZ, maxZ)));
        region.setMax(new Vector3d(Math.max(minX, maxX), maxY, Math.max(minZ, maxZ)));

        return region;
    }

    public RegionArea createCuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        RegionArea region = new RegionArea();

        region.setMin(new Vector3d(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ)));
        region.setMax(new Vector3d(Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ)));

        return region;
    }

    public RegionArea createCylinder(double centerX, double centerZ, double minY, double maxY, int radius) {
        RegionArea region = new RegionArea();

        region.setCenter(new Vector3d(centerX, 0d, centerZ));
        region.setMin(new Vector3d(0d, minY, 0d));
        region.setMax(new Vector3d(0d, maxY, 0d));
        region.setRadius(radius);
        region.recompute();

        return region;
    }

    public RegionArea createCylinderArea(double centerX, double centerZ, int radius) {
        RegionArea region = new RegionArea();

        region.setCenter(new Vector3d(centerX, 0d, centerZ));
        region.setRadius(radius);
        region.recompute();

        return region;
    }
}
