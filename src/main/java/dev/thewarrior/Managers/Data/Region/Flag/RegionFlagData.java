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

    /**
     * Verifica se um jogador tem permissão baseado nas keys de uma flag MAPPED.
     *
     * Lógica:
     * - Se wildcard "*" = true: permite todos, EXCETO se tiver permissão específica = false que o jogador possua
     * - Se wildcard "*" = false ou não definido: bloqueia todos, EXCETO se tiver permissão específica = true que o jogador possua
     *
     * @param flag Nome da flag (ex: "permissions")
     * @param permissionChecker Função que verifica se o jogador tem uma permissão (ex: player::hasPermission)
     * @return true se permitido, false se bloqueado
     */
    public boolean checkPlayerPermissions(String flag, java.util.function.Predicate<String> permissionChecker) {
        Map<String, Boolean> permissions = this.mappedFlags.get(flag.toLowerCase());

        if (permissions == null || permissions.isEmpty()) return true; // Sem configuração = permite

        Boolean wildcardValue = permissions.get("*");
        boolean defaultAllow = wildcardValue != null && wildcardValue;

        if (defaultAllow) {
            // Wildcard = true: permite por padrão, mas verifica se alguma permissão específica BLOQUEIA
            for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                if (entry.getKey().equals("*")) continue;

                // Se a permissão está marcada como FALSE e o jogador TEM essa permissão, bloqueia
                if (!entry.getValue() && permissionChecker.test(entry.getKey())) {
                    return false;
                }
            }

            return true;
        } else {
            // Wildcard = false ou não definido: bloqueia por padrão, mas verifica se alguma permissão específica PERMITE
            for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                if (entry.getKey().equals("*")) continue;

                // Se a permissão está marcada como TRUE e o jogador TEM essa permissão, permite
                if (entry.getValue() && permissionChecker.test(entry.getKey())) {
                    return true;
                }
            }

            return false;
        }
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

