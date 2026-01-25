package dev.thewarrior.Essentials.Utils;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;

public final class ColorUtil {
    private static final String[] COLOR_MAP = {
            "#000000", // &0 - Black
            "#0000AA", // &1 - Dark Blue
            "#00AA00", // &2 - Dark Green
            "#00AAAA", // &3 - Dark Aqua
            "#AA0000", // &4 - Dark Red
            "#AA00AA", // &5 - Dark Purple
            "#FFAA00", // &6 - Gold
            "#AAAAAA", // &7 - Gray
            "#555555", // &8 - Dark Gray
            "#5555FF", // &9 - Blue
            "#55FF55", // &a - Green
            "#55FFFF", // &b - Aqua
            "#FF5555", // &c - Red
            "#FF55FF", // &d - Light Purple
            "#FFFF55", // &e - Yellow
            "#FFFFFF"  // &f - White
    };

    // Limite máximo de fragmentos permitidos
    private static final int MAX_PARTS = 32;

    private ColorUtil() {}

    public static Message colorize(String text) {
        final int len = text.length();

        final Message[] parts = new Message[MAX_PARTS];
        int partCount = 0;

        String color = "#FFFFFF";
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean monospace = false;

        int segmentStart = 0;

        for (int i = 0; i < len - 1; i++) {
            if (text.charAt(i) != '&') {
                continue;
            }

            char next = text.charAt(i + 1);

            // ---------- HEX COLOR &#RRGGBB ----------
            if (next == '#' && i + 7 < len && isHex6(text, i + 2)) {
                if (i > segmentStart) {
                    if (partCount >= MAX_PARTS) return Message.raw(text);

                    parts[partCount++] = build(text.substring(segmentStart, i), color, bold, italic, underline, monospace);
                }

                color = "#" + text.substring(i + 2, i + 8).toUpperCase();
                i += 7;
                segmentStart = i + 1;
                continue;
            }

            char code = Character.toLowerCase(next);

            boolean isColor = isHex(code);
            boolean isStyle = code == 'l' || code == 'o' || code == 'n' || code == 'm' || code == 'r';

            if (!isColor && !isStyle) continue;

            if (i > segmentStart) {
                if (partCount >= MAX_PARTS) return Message.raw(text);

                parts[partCount++] = build(text.substring(segmentStart, i), color, bold, italic, underline, monospace);
            }

            if (isColor) {
                int idx = Character.digit(code, 16);

                color = COLOR_MAP[idx];
            } else {
                switch (code) {
                    case 'l' -> bold = true;
                    case 'o' -> italic = true;
                    case 'n' -> underline = true;
                    case 'm' -> monospace = true;
                    case 'r' -> {
                        color = "#FFFFFF";
                        bold = false;
                        italic = false;
                        underline = false;
                        monospace = false;
                    }
                }
            }

            i++;
            segmentStart = i + 1;
        }

        if (segmentStart < len) {
            if (partCount >= MAX_PARTS) {
                return Message.raw(text);
            }

            parts[partCount++] = build(text.substring(segmentStart), color, bold, italic, underline, monospace);
        }

        if (partCount == 0) {
            return Message.raw(text);
        }

        if (partCount == 1) {
            return parts[0];
        }

        Message[] finalParts = new Message[partCount];

        System.arraycopy(parts, 0, finalParts, 0, partCount);

        return Message.join(finalParts);
    }

    private static Message build(String text, String color, boolean bold, boolean italic, boolean underline, boolean monospace) {
        final Message msg = Message.raw(text).color(color).bold(bold).italic(italic);
        final FormattedMessage fmt = msg.getFormattedMessage();

        if (underline) {
            fmt.underlined = MaybeBool.True;
        }

        if (monospace) {
            fmt.monospace = MaybeBool.True;
        }

        return msg;
    }

    private static boolean isHex6(String s, int start) {
        for (int i = 0; i < 6; i++) {
            if (!isHex(s.charAt(start + i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}


