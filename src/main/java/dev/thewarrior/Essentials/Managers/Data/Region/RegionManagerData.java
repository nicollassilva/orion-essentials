package dev.thewarrior.Essentials.Managers.Data.Region;

import dev.thewarrior.Essentials.Managers.Data.Region.Composition.RegionType;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionGroupData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RegionManagerData {
    private final List<RegionGroupData> groups = new CopyOnWriteArrayList<>();
    private final List<RegionData> regions = new CopyOnWriteArrayList<>();

    public void addRegion(final RegionData regionData) {
        this.regions.add(regionData);
    }

    public void removeRegion(final RegionData regionData) {
        this.regions.remove(regionData);
    }

    public boolean hasRegionWithName(final String name) {
        return this.getRegionByName(name) != null;
    }

    public RegionData getRegionByName(final String name) {
        for (RegionData regionData : this.regions) {
            if(regionData.getName().equalsIgnoreCase(name)) {
                return regionData;
            }
        }

        return null;
    }

    public boolean hasGlobalRegionOnWorld(final String worldName) {
        for (RegionData regionData : this.regions) {
            if(regionData.getWorldName().equalsIgnoreCase(worldName) && regionData.getType().equals(RegionType.GLOBAL)) {
                return true;
            }
        }

        return false;
    }

    public List<RegionData> getRegions() {
        return this.regions;
    }
}
