package dev.thewarrior.Managers.Data.Region.Flag;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena todas as flags de uma região.
 *
 * Estrutura de dados:
 * - mappedFlags: Para flags do tipo MAPPED (key -> boolean)
 * - messageFlags: Para flags do tipo MESSAGE (string)
 * - booleanFlags: Para flags do tipo BOOLEAN (boolean)
 */
public class RegionFlagData {
    private final Map<String, Map<String, Boolean>> mappedFlags = new ConcurrentHashMap<>();
    private final Map<String, String> messageFlags = new ConcurrentHashMap<>();
    private final Map<String, Boolean> booleanFlags = new ConcurrentHashMap<>();

    // ==================== MAPPED FLAGS ====================

    /**
     * Define um valor mapeado para uma flag.
     * @param flag Nome da flag
     * @param key Chave (ex: "*" para wildcard, ou nome específico)
     * @param value Valor booleano
     */
    public void setMappedValue(String flag, String key, boolean value) {
        this.mappedFlags.computeIfAbsent(flag.toLowerCase(), k -> new ConcurrentHashMap<>()).put(key.toLowerCase(), value);
    }

    /**
     * Remove um valor mapeado de uma flag.
     */
    public void removeMappedValue(String flag, String key) {
        Map<String, Boolean> map = this.mappedFlags.get(flag.toLowerCase());
        if (map != null) {
            map.remove(key.toLowerCase());
            if (map.isEmpty()) {
                this.mappedFlags.remove(flag.toLowerCase());
            }
        }
    }

    /**
     * Verifica se uma ação é permitida baseado nas flags mapeadas.
     * Lógica de prioridade:
     * 1. Se existe uma regra específica para a key, usa ela
     * 2. Se não, usa a regra wildcard "*"
     * 3. Se não existe nenhuma regra, retorna null (não definido)
     */
    @Nullable
    public boolean checkMappedPermission(String flag, String key) {
        Map<String, Boolean> map = this.mappedFlags.get(flag.toLowerCase());

        if (map == null) return false;

        Boolean specific = map.get(key.toLowerCase());

        if (specific != null) return specific;

        return map.get("*");
    }

    /**
     * Retorna todos os valores mapeados de uma flag.
     */
    @Nullable
    public Map<String, Boolean> getMappedValues(String flag) {
        return this.mappedFlags.get(flag.toLowerCase());
    }

    // ==================== MESSAGE FLAGS ====================

    /**
     * Define uma mensagem para uma flag.
     */
    public void setMessage(String flag, String message) {
        this.messageFlags.put(flag.toLowerCase(), message);
    }

    /**
     * Remove uma mensagem de uma flag.
     */
    public void removeMessage(String flag) {
        this.messageFlags.remove(flag.toLowerCase());
    }

    /**
     * Obtém a mensagem de uma flag.
     */
    @Nullable
    public String getMessage(String flag) {
        return this.messageFlags.get(flag.toLowerCase());
    }


    // ==================== BOOLEAN FLAGS ====================

    /**
     * Define um valor booleano para uma flag.
     */
    public void setBoolean(String flag, boolean value) {
        this.booleanFlags.put(flag.toLowerCase(), value);
    }

    /**
     * Remove um valor booleano de uma flag.
     */
    public void removeBoolean(String flag) {
        this.booleanFlags.remove(flag.toLowerCase());
    }

    /**
     * Obtém o valor booleano de uma flag.
     */
    @Nullable
    public Boolean getBoolean(String flag) {
        return this.booleanFlags.get(flag.toLowerCase());
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Verifica se existe alguma configuração para uma flag.
     */
    public boolean hasFlag(String flagName) {
        String key = flagName.toLowerCase();

        return this.mappedFlags.containsKey(key) || this.messageFlags.containsKey(key) || this.booleanFlags.containsKey(key);
    }

    /**
     * Remove completamente uma flag.
     */
    public void clearFlag(String flagName) {
        String key = flagName.toLowerCase();

        this.mappedFlags.remove(key);
        this.messageFlags.remove(key);
        this.booleanFlags.remove(key);
    }

    /**
     * Verifica se não há nenhuma flag configurada.
     */
    public boolean isEmpty() {
        return this.mappedFlags.isEmpty() &&
               this.messageFlags.isEmpty() &&
               this.booleanFlags.isEmpty();
    }

    // ==================== GETTERS FOR SERIALIZATION ====================

    public Map<String, Map<String, Boolean>> getMappedFlags() {
        return this.mappedFlags;
    }

    public Map<String, String> getMessageFlags() {
        return this.messageFlags;
    }


    public Map<String, Boolean> getBooleanFlags() {
        return this.booleanFlags;
    }
}

