package dev.thewarrior.Essentials.Utils.Enums;

import dev.thewarrior.Essentials.Managers.WarpManager;

import java.util.function.Predicate;

public enum WarpValidationError {
    NONE("", name -> true),
    CANNOT_BE_EMPTY("O nome da warp não pode ser vazio.", String::isEmpty),
    MAX_LENGTH_EXCEEDED("O nome da warp excede o comprimento máximo permitido. (20 caracteres)", name -> name.length() > 20),
    ALPHANUMERIC_ONLY("O nome da warp deve conter apenas caracteres alfanuméricos.", name -> !WarpManager.WARP_NAME_PATTERN.matcher(name).matches()),

    ;

    private final String message;
    private Predicate<String> predicate;

    WarpValidationError(String message, Predicate<String> predicate) {
        this.message = message;
        this.predicate = predicate;
    }

    public static WarpValidationError validateWarpName(String name) {
        for (final WarpValidationError error : values()) {
            if(error.equals(WarpValidationError.NONE)) continue;

            if (error.getPredicate().test(name)) {
                return error;
            }
        }

        return NONE;
    }

    public String getMessage() {
        return message;
    }

    public Predicate<String> getPredicate() {
        return predicate;
    }
}
