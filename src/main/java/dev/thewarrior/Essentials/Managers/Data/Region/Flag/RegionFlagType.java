package dev.thewarrior.Essentials.Managers.Data.Region.Flag;

/**
 * Tipos de valores que uma flag pode ter.
 */
public enum RegionFlagType {
    /**
     * Flag que mapeia chaves para valores booleanos.
     * Exemplo: build -> { "*": false, "Chest": true }
     * Exemplo: commands -> { "*": true, "tell": false }
     */
    MAPPED,

    /**
     * Flag que armazena uma mensagem de texto.
     * Exemplo: greeting -> "Bem-vindo à região!"
     */
    MESSAGE,

    /**
     * Flag que armazena um valor booleano simples.
     * Exemplo: pvp -> true
     */
    BOOLEAN,

    /**
     * Tipo desconhecido (para flags não encontradas).
     */
    UNKNOWN
}

