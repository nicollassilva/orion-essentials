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

    // Rainbow colors for &r effect
    private static final String[] RAINBOW_COLORS = {
            "#FF0000", // Red
            "#FF7F00", // Orange
            "#FFFF00", // Yellow
            "#00FF00", // Green
            "#0000FF", // Blue
            "#4B0082", // Indigo
            "#9400D3"  // Violet
    };

    // Limite máximo de fragmentos permitidos
    private static final int MAX_PARTS = 256;

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

        // Special modes
        boolean rainbowMode = false;
        int rainbowIndex = 0;
        boolean gradientMode = false;
        String gradientStart = null;
        String gradientEnd = null;
        int gradientCharIndex = 0;
        int gradientTotalChars = 0;

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

                    if (rainbowMode) {
                        partCount = addRainbowText(parts, partCount, text.substring(segmentStart, i), rainbowIndex, bold, italic, underline, monospace);
                        rainbowIndex = (rainbowIndex + (i - segmentStart)) % RAINBOW_COLORS.length;
                    } else if (gradientMode) {
                        partCount = addGradientText(parts, partCount, text.substring(segmentStart, i), gradientStart, gradientEnd, gradientCharIndex, gradientTotalChars, bold, italic, underline, monospace);
                        gradientCharIndex += (i - segmentStart);
                    } else {
                        parts[partCount++] = build(text.substring(segmentStart, i), color, bold, italic, underline, monospace);
                    }
                }

                color = "#" + text.substring(i + 2, i + 8).toUpperCase();
                rainbowMode = false;
                gradientMode = false;
                i += 7;
                segmentStart = i + 1;
                continue;
            }

            // ---------- GRADIENT &g&color1&color2 ----------
            if (next == 'g' || next == 'G') {
                GradientParseResult gradient = tryParseGradient(text, i + 2);
                if (gradient != null) {
                    if (i > segmentStart) {
                        if (partCount >= MAX_PARTS) return Message.raw(text);

                        if (rainbowMode) {
                            partCount = addRainbowText(parts, partCount, text.substring(segmentStart, i), rainbowIndex, bold, italic, underline, monospace);
                        } else if (gradientMode) {
                            partCount = addGradientText(parts, partCount, text.substring(segmentStart, i), gradientStart, gradientEnd, gradientCharIndex, gradientTotalChars, bold, italic, underline, monospace);
                        } else {
                            parts[partCount++] = build(text.substring(segmentStart, i), color, bold, italic, underline, monospace);
                        }
                    }

                    rainbowMode = false;
                    gradientMode = true;
                    gradientStart = gradient.startColor;
                    gradientEnd = gradient.endColor;
                    gradientCharIndex = 0;
                    gradientTotalChars = countVisibleChars(text, i + 2 + gradient.consumedChars);

                    i += 1 + gradient.consumedChars;
                    segmentStart = i + 1;
                    continue;
                }
            }

            // ---------- RAINBOW &r ----------
            if (next == 'r' || next == 'R') {
                if (i > segmentStart) {
                    if (partCount >= MAX_PARTS) return Message.raw(text);

                    if (rainbowMode) {
                        partCount = addRainbowText(parts, partCount, text.substring(segmentStart, i), rainbowIndex, bold, italic, underline, monospace);
                        rainbowIndex = (rainbowIndex + (i - segmentStart)) % RAINBOW_COLORS.length;
                    } else if (gradientMode) {
                        partCount = addGradientText(parts, partCount, text.substring(segmentStart, i), gradientStart, gradientEnd, gradientCharIndex, gradientTotalChars, bold, italic, underline, monospace);
                        gradientCharIndex += (i - segmentStart);
                    } else {
                        parts[partCount++] = build(text.substring(segmentStart, i), color, bold, italic, underline, monospace);
                    }
                }

                rainbowMode = true;
                gradientMode = false;
                rainbowIndex = 0;
                i++;
                segmentStart = i + 1;
                continue;
            }

            char code = Character.toLowerCase(next);

            boolean isColor = isHex(code);
            boolean isStyle = code == 'l' || code == 'o' || code == 'n' || code == 'm' || code == 'x';

            if (!isColor && !isStyle) continue;

            if (i > segmentStart) {
                if (partCount >= MAX_PARTS) return Message.raw(text);

                if (rainbowMode) {
                    partCount = addRainbowText(parts, partCount, text.substring(segmentStart, i), rainbowIndex, bold, italic, underline, monospace);
                    rainbowIndex = (rainbowIndex + (i - segmentStart)) % RAINBOW_COLORS.length;
                } else if (gradientMode) {
                    partCount = addGradientText(parts, partCount, text.substring(segmentStart, i), gradientStart, gradientEnd, gradientCharIndex, gradientTotalChars, bold, italic, underline, monospace);
                    gradientCharIndex += (i - segmentStart);
                } else {
                    parts[partCount++] = build(text.substring(segmentStart, i), color, bold, italic, underline, monospace);
                }
            }

            if (isColor) {
                int idx = Character.digit(code, 16);

                color = COLOR_MAP[idx];
                rainbowMode = false;
                gradientMode = false;
            } else {
                switch (code) {
                    case 'l' -> bold = true;
                    case 'o' -> italic = true;
                    case 'n' -> underline = true;
                    case 'm' -> monospace = true;
                    case 'x' -> {
                        color = "#FFFFFF";
                        bold = false;
                        italic = false;
                        underline = false;
                        monospace = false;
                        rainbowMode = false;
                        gradientMode = false;
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

            if (rainbowMode) {
                partCount = addRainbowText(parts, partCount, text.substring(segmentStart), rainbowIndex, bold, italic, underline, monospace);
            } else if (gradientMode) {
                partCount = addGradientText(parts, partCount, text.substring(segmentStart), gradientStart, gradientEnd, gradientCharIndex, gradientTotalChars, bold, italic, underline, monospace);
            } else {
                parts[partCount++] = build(text.substring(segmentStart), color, bold, italic, underline, monospace);
            }
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

    // Cache de strings para caracteres únicos (evita String.valueOf repetido)
    private static final String[] CHAR_STRINGS = new String[128];

    static {
        for (int i = 0; i < 128; i++) {
            CHAR_STRINGS[i] = String.valueOf((char) i);
        }
    }

    private static Message buildChar(char c, String color, boolean bold, boolean italic, boolean underline, boolean monospace) {
        String text = (c < 128) ? CHAR_STRINGS[c] : String.valueOf(c);
        return build(text, color, bold, italic, underline, monospace);
    }

    // ---------- RAINBOW SUPPORT ----------
    private static int addRainbowText(Message[] parts, int partCount, String text, int startIndex, boolean bold, boolean italic, boolean underline, boolean monospace) {
        final int textLen = text.length();
        if (textLen == 0) return partCount;

        int colorIndex = startIndex;
        int i = 0;

        while (i < textLen && partCount < MAX_PARTS) {
            char c = text.charAt(i);

            // Agrupa espaços consecutivos
            if (c == ' ') {
                int spaceStart = i;
                while (i < textLen && text.charAt(i) == ' ') {
                    i++;
                }
                parts[partCount++] = build(text.substring(spaceStart, i), "#FFFFFF", bold, italic, underline, monospace);
            } else {
                // Cada caractere não-espaço recebe sua própria cor
                String rainbowColor = RAINBOW_COLORS[colorIndex % RAINBOW_COLORS.length];
                parts[partCount++] = buildChar(c, rainbowColor, bold, italic, underline, monospace);
                colorIndex++;
                i++;
            }
        }
        return partCount;
    }

    // ---------- GRADIENT SUPPORT ----------
    private static int addGradientText(Message[] parts, int partCount, String text, String startColor, String endColor, int charIndex, int totalChars, boolean bold, boolean italic, boolean underline, boolean monospace) {
        final int textLen = text.length();
        if (textLen == 0) return partCount;

        if (totalChars <= 1) {
            if (partCount < MAX_PARTS) {
                parts[partCount++] = build(text, startColor, bold, italic, underline, monospace);
            }
            return partCount;
        }

        // Parse RGB uma vez só
        final int startR = parseHexComponent(startColor, 1);
        final int startG = parseHexComponent(startColor, 3);
        final int startB = parseHexComponent(startColor, 5);
        final int deltaR = parseHexComponent(endColor, 1) - startR;
        final int deltaG = parseHexComponent(endColor, 3) - startG;
        final int deltaB = parseHexComponent(endColor, 5) - startB;

        final float divisor = totalChars - 1;
        int i = 0;

        while (i < textLen && partCount < MAX_PARTS) {
            char c = text.charAt(i);

            // Agrupa espaços consecutivos
            if (c == ' ') {
                int spaceStart = i;
                while (i < textLen && text.charAt(i) == ' ') {
                    charIndex++;
                    i++;
                }
                parts[partCount++] = build(text.substring(spaceStart, i), "#FFFFFF", bold, italic, underline, monospace);
            } else {
                float ratio = charIndex / divisor;
                String interpolatedColor = interpolateColorFast(startR, startG, startB, deltaR, deltaG, deltaB, ratio);
                parts[partCount++] = buildChar(c, interpolatedColor, bold, italic, underline, monospace);
                charIndex++;
                i++;
            }
        }
        return partCount;
    }

    private static int parseHexComponent(String hex, int offset) {
        int high = Character.digit(hex.charAt(offset), 16);
        int low = Character.digit(hex.charAt(offset + 1), 16);
        return (high << 4) | low;
    }

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private static String interpolateColorFast(int startR, int startG, int startB, int deltaR, int deltaG, int deltaB, float ratio) {
        int r = Math.round(startR + deltaR * ratio);
        int g = Math.round(startG + deltaG * ratio);
        int b = Math.round(startB + deltaB * ratio);

        // Constrói a string hex diretamente sem String.format
        return new String(new char[]{
                '#',
                HEX_CHARS[(r >> 4) & 0xF], HEX_CHARS[r & 0xF],
                HEX_CHARS[(g >> 4) & 0xF], HEX_CHARS[g & 0xF],
                HEX_CHARS[(b >> 4) & 0xF], HEX_CHARS[b & 0xF]
        });
    }

    // ---------- GRADIENT PARSING ----------
    private static class GradientParseResult {
        String startColor;
        String endColor;
        int consumedChars;

        GradientParseResult(String start, String end, int consumed) {
            this.startColor = start;
            this.endColor = end;
            this.consumedChars = consumed;
        }
    }

    private static GradientParseResult tryParseGradient(String text, int startPos) {
        if (startPos >= text.length()) return null;

        String color1 = tryParseColor(text, startPos);
        if (color1 == null) return null;

        int nextPos = startPos + getColorCodeLength(text, startPos);

        String color2 = tryParseColor(text, nextPos);
        if (color2 == null) return null;

        int totalConsumed = getColorCodeLength(text, startPos) + getColorCodeLength(text, nextPos);

        return new GradientParseResult(color1, color2, totalConsumed);
    }

    private static String tryParseColor(String text, int pos) {
        if (pos >= text.length() || text.charAt(pos) != '&') return null;
        pos++;

        if (pos >= text.length()) return null;

        char next = text.charAt(pos);

        // &#RRGGBB format
        if (next == '#' && pos + 6 < text.length() && isHex6(text, pos + 1)) {
            return "#" + text.substring(pos + 1, pos + 7).toUpperCase();
        }

        // &0-&f format
        char code = Character.toLowerCase(next);
        if (isHex(code)) {
            int idx = Character.digit(code, 16);
            return COLOR_MAP[idx];
        }

        return null;
    }

    private static int getColorCodeLength(String text, int pos) {
        if (pos >= text.length() || text.charAt(pos) != '&') return 0;

        if (pos + 1 >= text.length()) return 0;

        char next = text.charAt(pos + 1);

        // &#RRGGBB = 8 characters
        if (next == '#' && pos + 7 < text.length() && isHex6(text, pos + 2)) {
            return 8;
        }

        // &X = 2 characters
        if (isHex(Character.toLowerCase(next))) {
            return 2;
        }

        return 0;
    }

    private static int countVisibleChars(String text, int startPos) {
        int count = 0;
        for (int i = startPos; i < text.length(); i++) {
            if (text.charAt(i) == '&' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);

                // Check for gradient, rainbow, or other codes that end gradient mode
                if (next == 'g' || next == 'G' || next == 'r' || next == 'R') {
                    break;
                }

                // Check for hex color &#RRGGBB
                if (next == '#' && i + 7 < text.length() && isHex6(text, i + 2)) {
                    break;
                }

                // Check for color code &0-&f
                char code = Character.toLowerCase(next);
                if (isHex(code)) {
                    break;
                }

                // Check for style codes that don't break gradient
                if (code == 'l' || code == 'o' || code == 'n' || code == 'm') {
                    i++; // skip style code
                    continue;
                }

                // Check for reset &x
                if (code == 'x') {
                    break;
                }
            }
            count++;
        }
        return count;
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


