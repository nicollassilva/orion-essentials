package dev.thewarrior.Managers;

import com.hypixel.hytale.math.vector.Vector3i;
import dev.thewarrior.Managers.Composition.StorableManager;
import dev.thewarrior.Managers.Data.Region.Composition.RegionType;
import dev.thewarrior.Managers.Data.Region.Data.RegionArea;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.Data.Region.Factory.RegionAreaFactory;
import dev.thewarrior.Managers.Data.Region.RegionManagerData;

import javax.annotation.Nonnull;
import java.io.InvalidObjectException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class RegionManager extends StorableManager<RegionManagerData> {
    private final RegionAreaFactory regionAreaFactory;

    public RegionManager(@Nonnull Path dataFolder) {
        super(dataFolder, "regions.json", RegionManagerData.class);

        this.regionAreaFactory = new RegionAreaFactory();
    }

    @Override
    protected RegionManagerData createDefaultData() {
        return new RegionManagerData();
    }

    public boolean hasRegionWithName(final String name) {
        return this.data.hasRegionWithName(name);
    }

    public boolean hasGlobalRegionOnWorld(final String worldName) {
        return this.data.hasGlobalRegionOnWorld(worldName);
    }

    public List<RegionData> getRegions() {
        return this.data.getRegions();
    }

    public RegionData getRegionByName(final String name) {
        return this.data.getRegionByName(name);
    }

    public RegionData create(RegionType type, String name, String worldName, Vector3i min, Vector3i max, int priority) throws Exception {
        final RegionArea area = switch (type) {
            case GLOBAL -> this.regionAreaFactory.createGlobal();
            case AREA -> this.regionAreaFactory.createArea(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            case CUBOID ->  this.regionAreaFactory.createCuboid(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            default -> null;
        };

        if(area == null) {
            throw new InvalidObjectException("Invalid region type provided for region creation: " + type);
        }

        if(type.equals(RegionType.GLOBAL)) {
            priority = -1;
        }

        final RegionData data = new RegionData(UUID.randomUUID(), worldName, name, type, priority, area, System.currentTimeMillis());

        this.data.addRegion(data);
        this.saveConfig();

        return data;
    }
}
