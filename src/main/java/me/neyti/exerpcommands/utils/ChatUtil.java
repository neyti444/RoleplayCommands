package me.neyti.exerpcommands.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neyti.exerpcommands.ExeRpCommands;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилитный класс для отправки сообщений с поддержкой PlaceholderAPI, HEX и стандартных & цветовых кодов.
 */
public class ChatUtil {

    // Шаблон для поиска HEX-цветов, например &#FFFFFF
    private static final Pattern HEX_PATTERN = Pattern.compile("\\&#([A-Fa-f0-9]{6})");

    /**
     * Отправляет сообщение получателю, обрабатывая PlaceholderAPI, HEX и & цветовые коды.
     *
     * @param plugin   Главный класс плагина.
     * @param receiver Получатель сообщения (игрок или консоль).
     * @param rawText  Исходный текст сообщения.
     */
    public static void sendMessage(ExeRpCommands plugin, CommandSender receiver, String rawText) {
        if (rawText == null) return;

        // 1. PlaceholderAPI: заменяем плейсхолдеры, если плагин установлен и получатель - игрок.
        if (receiver instanceof Player) {
            Player player = (Player) receiver;
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                rawText = PlaceholderAPI.setPlaceholders(player, rawText);
            }
        }

        // 2. Обработка HEX-цветов, если включены и сервер >=1.16
        boolean hexEnabled = plugin.getConfig().getBoolean("hex-colors-enable", false);
        boolean isModern = isModernApi();

        if (hexEnabled && isModern) {
            rawText = replaceHex(rawText);
        }

        // 3. Обработка стандартных & цветовых кодов
        String processedText = ChatColor.translateAlternateColorCodes('&', rawText);

        // 4. Отправка сообщения
        receiver.sendMessage(processedText);
    }

    /**
     * Заменяет все &#RRGGBB на §x§R§R§G§G§B§B.
     * /
     * @param input Исходный текст.
     * @return Текст с заменёнными HEX-цветами.
     */
    private static String replaceHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(Character.toLowerCase(c));
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Проверяет, является ли версия сервера >=1.16.
     *
     * @return true, если версия >=1.16, иначе false.
     */
    public static boolean isModernApi() {
        String ver = Bukkit.getBukkitVersion(); // Например, "1.20.1-R0.1-SNAPSHOT"
        // Простая проверка
        if (ver.startsWith("1.7") || ver.startsWith("1.8") || ver.startsWith("1.9") ||
                ver.startsWith("1.10") || ver.startsWith("1.11") || ver.startsWith("1.12") ||
                ver.startsWith("1.13") || ver.startsWith("1.14") || ver.startsWith("1.15")) {
            return false;
        }
        return true;
    }
}
