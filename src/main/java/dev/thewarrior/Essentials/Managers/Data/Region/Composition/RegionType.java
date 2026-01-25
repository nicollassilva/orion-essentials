package dev.thewarrior.Essentials.Managers.Data.Region.Composition;

public enum RegionType {
    NO_FOUND("Não encontrado"),

    GLOBAL("Todo o mundo"),
    AREA("Área especificada, mas com altura infinita"),
    CUBOID("Apenas a área selecionada"),
    ;

    private final String description;

    RegionType(final String description) {
        this.description = description;
    }

    public static RegionType fromString(final String type) {
        for (final RegionType regionType : values()) {
            if (regionType.name().equalsIgnoreCase(type)) {
                return regionType;
            }
        }

        return NO_FOUND;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDetail() {
        return this.name() + " (" + this.description + ")";
    }
}
