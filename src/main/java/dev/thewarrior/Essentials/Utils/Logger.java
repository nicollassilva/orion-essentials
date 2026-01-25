package dev.thewarrior.Essentials.Utils;

import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Logger {
    private static HytaleLogger logger;
    private static String prefix = "[OrionEssentials] ";

    // ANSI color codes
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private Logger() {}

    public static void init(@Nonnull HytaleLogger logger) {
        Logger.logger = logger;
    }

    public static void info(@Nonnull String message) {
        logger.at(Level.INFO).log(prefix + GREEN + message + RESET);
    }

    public static void warning(@Nonnull String message) {
        logger.at(Level.WARNING).log(prefix + YELLOW + message + RESET);
    }

    public static void error(@Nonnull String message) {
        logger.at(Level.SEVERE).log(prefix + RED + message + RESET);
    }

    public static void error(@Nonnull String message, @Nonnull Throwable throwable) {
        logger.at(Level.SEVERE).withCause(throwable).log(prefix + RED + message + RESET);
    }
}
