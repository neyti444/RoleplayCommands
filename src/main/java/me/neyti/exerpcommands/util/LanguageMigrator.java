package me.neyti.exerpcommands.util;

import me.neyti.exerpcommands.ExeRpCommands;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Миграция языковых файлов (en.yml, ru.yml) до целевой lang-version из ресурсов.
 * - Не трогаем существующие пользовательские значения.
 * - Добавляем недостающие ключи из дефолтов.
 * - Переименовываем известные старые ключи (копируя значение).
 * - Делаем backup перед сохранением.
 * - Пишем header-инструкцию (Bukkit сохраняет только header, inline-комментарии теряются).
 */
public final class LanguageMigrator {

    private LanguageMigrator() {}

    /** Прогон миграции для конкретного кода языка. */
    public static boolean migrate(ExeRpCommands plugin, String code) {
        File data = plugin.getDataFolder();
        if (!data.exists()) data.mkdirs();

        File file = new File(data, code + ".yml");
        if (!file.exists()) {
            // если файла нет — просто распакуем дефолт
            saveResource(plugin, code + ".yml");
            plugin.getLogger().info("[Lang] " + code + ".yml created from defaults.");
            return true;
        }

        // загрузим пользовательский и дефолтный файлы
        YamlConfiguration user = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defs = loadFromJar(plugin, code + ".yml");
        if (defs == null) {
            // если вдруг нет дефолта конкретного языка — используем en.yml как базу
            plugin.getLogger().warning("[Lang] Default " + code + ".yml not found in jar. Falling back to en.yml defaults.");
            defs = loadFromJar(plugin, "en.yml");
            if (defs == null) {
                plugin.getLogger().warning("[Lang] en.yml defaults not found either. Skip " + code + " migration.");
                return false;
            }
        }

        int current = user.getInt("lang-version", 1);
        int target  = defs.getInt("lang-version", current);

        boolean changed = false;

        // миграции по версиям (пошагово)
        for (int v = current; v < target; v++) {
            switch (v) {
                case 1:
                    changed |= migrate1to2(plugin, user);
                    user.set("lang-version", 2);
                    break;
                // тут же добавляй следующие версии при необходимости:
                // case 2:
                //     changed |= migrate2to3(plugin, user);
                //     user.set("lang-version", 3);
                //     break;
                default:
                    // нет явного шага — просто сдвигаем версию (недостающие ключи добавим из дефолтов ниже)
                    user.set("lang-version", v + 1);
            }
        }

        // Добавляем недостающие ключи из дефолтов (значения берём из дефолта), текущие — не трогаем
        changed |= fillMissingFromDefaults(user, defs);

        if (changed) {
            backup(file, plugin);
            // устанавливаем понятный header
            String header = buildHeaderFor(code);
            user.options().header(header);
            user.options().copyHeader(true);
            try {
                user.save(file);
                plugin.getLogger().info("[Lang] " + code + ".yml migrated to v" + user.getInt("lang-version", target));
            } catch (IOException e) {
                plugin.getLogger().warning("[Lang] Failed to save " + code + ".yml: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("[Lang] " + code + ".yml already up to date (v" + user.getInt("lang-version", target) + ").");
        }
        return changed;
    }

    /** v1 → v2: переименования и доп. совместимость. */
    private static boolean migrate1to2(ExeRpCommands plugin, YamlConfiguration user) {
        boolean changed = false;

        // messages.reload -> messages.reloaded (копируем значение, старый ключ НЕ удаляем)
        if (!user.contains("messages.reloaded") && user.contains("messages.reload")) {
            user.set("messages.reloaded", user.getString("messages.reload"));
            changed = true;
            plugin.getLogger().info("[Lang] Renamed messages.reload → messages.reloaded");
        }

        // некоторые старые переводы могли иметь чуть другие ключи системных сообщений
        // здесь можно добавлять копирования при необходимости (пример):
        if (!user.contains("messages.invalid_number") && user.contains("messages.invalidnumber")) {
            user.set("messages.invalid_number", user.getString("messages.invalidnumber"));
            changed = true;
            plugin.getLogger().info("[Lang] Renamed messages.invalidnumber → messages.invalid_number");
        }

        return changed;
    }

    /** Заполняем отсутствующие листовые ключи из дефолтов. */
    private static boolean fillMissingFromDefaults(YamlConfiguration user, YamlConfiguration defs) {
        boolean changed = false;
        Set<String> defaultKeys = defs.getKeys(true).stream()
                .filter(k -> !defs.isConfigurationSection(k))
                .collect(Collectors.toSet());

        for (String key : defaultKeys) {
            if (!user.contains(key)) {
                user.set(key, defs.get(key));
                changed = true;
            }
        }
        // если нет lang-version вообще — проставим из дефолта
        if (!user.contains("lang-version") && defs.contains("lang-version")) {
            user.set("lang-version", defs.getInt("lang-version", 1));
            changed = true;
        }
        return changed;
    }

    private static YamlConfiguration loadFromJar(ExeRpCommands plugin, String resource) {
        try (InputStream in = plugin.getResource(resource)) {
            if (in == null) return null;
            try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return YamlConfiguration.loadConfiguration(r);
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    private static void saveResource(ExeRpCommands plugin, String name) {
        if (plugin.getResource(name) != null) {
            plugin.saveResource(name, false);
        }
    }

    private static void backup(File file, ExeRpCommands plugin) {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File bak = new File(file.getParentFile(), file.getName() + ".bak-" + stamp);
        try {
            java.nio.file.Files.copy(file.toPath(), bak.toPath());
            plugin.getLogger().info("[Lang] Backup created: " + bak.getName());
        } catch (IOException e) {
            plugin.getLogger().warning("[Lang] Backup failed for " + file.getName() + ": " + e.getMessage());
        }
    }

    private static String buildHeaderFor(String code) {
        if ("ru".equalsIgnoreCase(code)) {
            return ""
                    + "RU перевод\n"
                    + "\n"
                    + "Переменные:\n"
                    + "{player} – ник игрока\n"
                    + "{message} – сообщение игрока\n"
                    + "{rollNumber} – выпавшее число /roll\n"
                    + "{defaultMinNumber} – минимальное число по умолчанию /roll\n"
                    + "{defaultMaxNumber} – максимальное число по умолчанию /roll\n"
                    + "{minNumber} – минимум в расширенном /roll\n"
                    + "{maxNumber} – максимум в расширенном /roll\n"
                    + "{messageOne} – первое сообщение /todo\n"
                    + "{messageTwo} – второе сообщение /todo\n"
                    + "\n"
                    + "Поддерживаются &-коды и HEX (1.16+), напр.: &#FB0000HELLO\n";
        }
        // EN
        return ""
                + "EN translation\n"
                + "\n"
                + "Variables:\n"
                + "{player} – player name\n"
                + "{message} – player message\n"
                + "{rollNumber} – rolled number /roll\n"
                + "{defaultMinNumber} – default minimum /roll\n"
                + "{defaultMaxNumber} – default maximum /roll\n"
                + "{minNumber} – min in extended /roll\n"
                + "{maxNumber} – max in extended /roll\n"
                + "{messageOne} – first part /todo\n"
                + "{messageTwo} – second part /todo\n"
                + "\n"
                + "Supports &-colors and HEX (1.16+), e.g. &#FB0000HELLO\n";
    }
}
