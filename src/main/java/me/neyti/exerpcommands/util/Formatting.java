package me.neyti.exerpcommands.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Одноразовое применение раскраски (& + #RRGGBB на 1.16+), без зависимости от ChatColor.of(...) */
public final class Formatting {
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    private Formatting() {}

    /** Применяет &-коды и, если включено и версия позволяет, конвертирует #RRGGBB в §x§R§R§G§G§B§B. */
    public static String applyAllOnce(String input, boolean hexEnabled) {
        if (input == null || input.isEmpty()) return input;

        // 1) &-коды
        String out = ChatColor.translateAlternateColorCodes('&', input);

        // 2) HEX -> §x§R§R§G§G§B§B (без ChatColor.of, совместимо на уровне компиляции)
        if (hexEnabled && isModern()) {
            out = replaceHexWithSectionFormat(out);
        }
        return out;
    }

    /** Заменяет все #RRGGBB на §x§R§R§G§G§B§B */
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

    /** Собирает строку формата §x§R§R§G§G§B§B для заданного hex без решётки. */
    private static String toLegacySectionHex(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            builder.append('§').append(Character.toLowerCase(c));
        }
        return builder.toString();
    }

    /** Грубо считаем «современной» 1.16+; для них работает §x-формат. */
    private static boolean isModern() {
        String v = Bukkit.getBukkitVersion(); // "1.20.6-R0.1-SNAPSHOT"
        return !(v.startsWith("1.7") || v.startsWith("1.8") || v.startsWith("1.9") ||
                v.startsWith("1.10") || v.startsWith("1.11") || v.startsWith("1.12") ||
                v.startsWith("1.13") || v.startsWith("1.14") || v.startsWith("1.15"));
    }
}
