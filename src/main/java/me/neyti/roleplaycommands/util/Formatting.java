package me.neyti.roleplaycommands.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Formatting {
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    private Formatting() {}

    public static String applyAllOnce(String input, boolean hexEnabled) {
        if (input == null || input.isEmpty()) return input;

        String out = ChatColor.translateAlternateColorCodes('&', input);

        if (hexEnabled && isModern()) {
            out = replaceHexWithSectionFormat(out);
        }
        return out;
    }

    private static String replaceHexWithSectionFormat(String text) {
        Matcher m = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1);
            m.appendReplacement(sb, toLegacySectionHex(hex));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String toLegacySectionHex(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            builder.append('§').append(Character.toLowerCase(c));
        }
        return builder.toString();
    }

    private static boolean isModern() {
        String v = Bukkit.getBukkitVersion(); // "1.20.6-R0.1-SNAPSHOT"
        return !(v.startsWith("1.7") || v.startsWith("1.8") || v.startsWith("1.9") ||
                v.startsWith("1.10") || v.startsWith("1.11") || v.startsWith("1.12") ||
                v.startsWith("1.13") || v.startsWith("1.14") || v.startsWith("1.15"));
    }
}
