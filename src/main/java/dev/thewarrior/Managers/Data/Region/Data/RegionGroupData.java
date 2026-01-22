package dev.thewarrior.Managers.Data.Region.Data;

import java.util.UUID;

public class RegionGroupData {
    private final String name;
    private final UUID id;

    public RegionGroupData(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }
}
