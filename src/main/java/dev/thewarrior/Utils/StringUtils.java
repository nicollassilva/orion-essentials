package dev.thewarrior.Utils;

public class StringUtils {
    public static String padLeft(String str, int length, String padStr) {
        return padStr.repeat(length) + str;
    }
}
