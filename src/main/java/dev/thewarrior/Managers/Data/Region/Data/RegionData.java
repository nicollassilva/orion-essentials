package dev.thewarrior.Managers.Data.Region.Data;

import dev.thewarrior.Managers.Data.Region.Composition.RegionType;

import java.util.UUID;

/**
 * Priority logic:
 * * - Global regions have priority -1
 * * - Higher priority regions override lower priority regions
 * * - Regions with the same priority override each other based on creation time (newer regions override older regions)
 */
public class RegionData {
    private final UUID id;
    private final String worldName;
    private String name;
    private RegionType type;
    private int priority;
    private RegionArea area;
    private long creationTime;

    public RegionData(UUID id, String worldName, String name, RegionType type, int priority, RegionArea area, long creationTime) {
        this.id = id;
        this.worldName = worldName;
        this.name = name;
        this.type = type;
        this.priority = priority;
        this.area = area;
        this.creationTime = creationTime;
    }

    public UUID getId() {
        return this.id;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RegionType getType() {
        return this.type;
    }

    public void setType(RegionType type) {
        this.type = type;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public RegionArea getArea() {
        return this.area;
    }

    public void setArea(RegionArea area) {
        this.area = area;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getBounds() {
        if(this.area == null) {
            return "&4[ALERTA] &rÁrea não definida! Isso não deveria acontecer.";
        }

        if(this.area.hasEmptyBounds()) return "N/A";

        return "&6Min: &r" + this.area.getMinString(this.type) + "\n" +
               "&6Max: &r" + this.area.getMaxString(this.type) +
                (this.area.getCenter() != null ? "\n&6Center: &r" + this.area.getCenterString() : "");
    }
}
