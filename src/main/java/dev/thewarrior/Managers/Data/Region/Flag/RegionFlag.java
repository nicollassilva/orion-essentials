package dev.thewarrior.Managers.Data.Region.Flag;

import javax.annotation.Nullable;

public enum RegionFlag {
    // Flags do tipo MAPPED (key -> boolean)
    BUILD("build", "Controla permissões de construção", RegionFlagType.MAPPED),
    INTERACT("interact", "Controla permissões de interação com blocos/entidades", RegionFlagType.MAPPED),
    BREAK("break", "Controla permissões de destruição de blocos", RegionFlagType.MAPPED),
    USE_ITEM("use", "Controla permissões de uso de itens", RegionFlagType.MAPPED), // Tomar poções, comer comida, etc.
    PICKUP_ITEM("pickup", "Controla permissões de pegar itens", RegionFlagType.MAPPED),
    COMMANDS("commands", "Controla permissões de comandos", RegionFlagType.MAPPED),
    PERMISSIONS("permissions", "Controla permissões especiais", RegionFlagType.MAPPED),

    // Flags do tipo MESSAGE
    GREETING("greeting", "Mensagem exibida ao entrar na região", RegionFlagType.MESSAGE),
    FAREWELL("farewell", "Mensagem exibida ao sair da região", RegionFlagType.MESSAGE),

    // Flags do tipo BOOLEAN
    PVP("pvp", "Permite ou bloqueia PvP na região", RegionFlagType.BOOLEAN),
    PVM("pvm", "Permite ou bloqueia dano de monstros na região", RegionFlagType.BOOLEAN),
    PVE("pve", "Permite ou bloqueia dano ambiental na região", RegionFlagType.BOOLEAN),
    INVINCIBLE("invincible", "Jogadores são invencíveis na região", RegionFlagType.BOOLEAN),
    MOB_SPAWNING("mob_spawning", "Permite spawn de mobs na região", RegionFlagType.BOOLEAN),
    FALL_DAMAGE("fall_damage", "Permite dano de queda na região", RegionFlagType.BOOLEAN),
    DROP("drop", "Permite dropar itens na região", RegionFlagType.BOOLEAN),

    // Flag não encontrada
    NOT_FOUND("not_found", "Flag não encontrada", RegionFlagType.UNKNOWN);

    private final String name;
    private final String description;
    private final RegionFlagType type;

    RegionFlag(String name, String description, RegionFlagType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public RegionFlagType getType() {
        return this.type;
    }

    @Nullable
    public static RegionFlag fromString(String name) {
        for (RegionFlag flag : values()) {
            if (flag.getName().equalsIgnoreCase(name)) {
                return flag;
            }
        }
        return NOT_FOUND;
    }

    public static RegionFlag[] getValidFlags() {
        RegionFlag[] flags = values();
        RegionFlag[] validFlags = new RegionFlag[flags.length - 1];
        int index = 0;

        for (RegionFlag flag : flags) {
            if (flag != NOT_FOUND) {
                validFlags[index++] = flag;
            }
        }

        return validFlags;
    }
}
