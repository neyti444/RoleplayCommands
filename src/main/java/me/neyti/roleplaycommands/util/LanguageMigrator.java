package me.neyti.roleplaycommands.util;

import me.neyti.roleplaycommands.RoleplayCommands;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public final class LanguageMigrator {

    private LanguageMigrator() {}

    public static boolean migrate(RoleplayCommands plugin, String code) {
        File data = plugin.getDataFolder();
        if (!data.exists()) data.mkdirs();

        File file = new File(data, code + ".yml");
        if (!file.exists()) {
            saveResource(plugin, code + ".yml");
            plugin.getLogger().info("[Lang] " + code + ".yml created from defaults.");
            return true;
        }

        YamlConfiguration user = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defs = loadFromJar(plugin, code + ".yml");
        if (defs == null) {
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

        for (int v = current; v < target; v++) {
            switch (v) {
                case 1:
                    changed |= migrate1to2(plugin, user);
                    user.set("lang-version", 2);
                    break;
                default:
                    user.set("lang-version", v + 1);
            }
        }

        changed |= fillMissingFromDefaults(user, defs);

        if (changed) {
            backup(file, plugin);
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

    private static boolean migrate1to2(RoleplayCommands plugin, YamlConfiguration user) {
        boolean changed = false;

        if (!user.contains("messages.reloaded") && user.contains("messages.reload")) {
            user.set("messages.reloaded", user.getString("messages.reload"));
            changed = true;
            plugin.getLogger().info("[Lang] Renamed messages.reload → messages.reloaded");
        }

        if (!user.contains("messages.invalid_number") && user.contains("messages.invalidnumber")) {
            user.set("messages.invalid_number", user.getString("messages.invalidnumber"));
            changed = true;
            plugin.getLogger().info("[Lang] Renamed messages.invalidnumber → messages.invalid_number");
        }

        return changed;
    }

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
        if (!user.contains("lang-version") && defs.contains("lang-version")) {
            user.set("lang-version", defs.getInt("lang-version", 1));
            changed = true;
        }
        return changed;
    }

    private static YamlConfiguration loadFromJar(RoleplayCommands plugin, String resource) {
        try (InputStream in = plugin.getResource(resource)) {
            if (in == null) return null;
            try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return YamlConfiguration.loadConfiguration(r);
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    private static void saveResource(RoleplayCommands plugin, String name) {
        if (plugin.getResource(name) != null) {
            plugin.saveResource(name, false);
        }
    }

    private static void backup(File file, RoleplayCommands plugin) {
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
