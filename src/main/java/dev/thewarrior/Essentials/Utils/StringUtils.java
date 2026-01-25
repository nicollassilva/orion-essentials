package dev.thewarrior.Essentials.Utils;

public class StringUtils {
    // 59
    public static String padLeft(String str, int length, String padStr) {
        return padStr.repeat(length) + str;
    }
}
