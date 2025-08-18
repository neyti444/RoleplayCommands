package me.neyti.exerpcommands.util;

import me.neyti.exerpcommands.ExeRpCommands;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Последовательные миграции config.yml по config-version. */
public final class ConfigMigrator {

    private ConfigMigrator() {}

    public static boolean migrate(ExeRpCommands plugin, FileConfiguration cfg, YamlConfiguration defaults) {
        int current = cfg.getInt("config-version", 1);
        int target  = defaults.getInt("config-version", current);

        if (current >= target) {
            plugin.getLogger().info("[Config] Up to date (version " + current + ").");
            return false;
        }

        backupConfig(plugin);

        boolean changed = false;
        for (int v = current; v < target; v++) {
            switch (v) {
                case 1:
                    changed |= migrate1to2(plugin, cfg);
                    cfg.set("config-version", 2);
                    break;
                case 2:
                    changed |= migrate2to3(plugin, cfg);
                    cfg.set("config-version", 3);
                    break;
                default:
                    plugin.getLogger().warning("[Config] No explicit migrator for " + v + " → " + (v+1) + ". Using defaults merge.");
                    cfg.set("config-version", v + 1);
            }
        }
        return changed;
    }

    /** Старая универсальная миграция 1 → 2 (оставляем на будущее). */
    private static boolean migrate1to2(ExeRpCommands plugin, FileConfiguration cfg) {
        boolean changed = false;
        if (!cfg.contains("language") && cfg.contains("lang")) {
            cfg.set("language", cfg.get("lang"));
            changed = true;
            plugin.getLogger().info("[Config] Renamed 'lang' → 'language'");
        }
        if (!cfg.contains("hex-colors-enable")) {
            if (cfg.contains("hex-colors")) {
                cfg.set("hex-colors-enable", cfg.getBoolean("hex-colors", true));
                changed = true;
                plugin.getLogger().info("[Config] Renamed 'hex-colors' → 'hex-colors-enable'");
            } else if (cfg.contains("use-hex-colors")) {
                cfg.set("hex-colors-enable", cfg.getBoolean("use-hex-colors", true));
                changed = true;
                plugin.getLogger().info("[Config] Renamed 'use-hex-colors' → 'hex-colors-enable'");
            }
        }
        return changed;
    }

    /** Миграция твоего релиза 1.1b (config-version: 2) → v3. */
    private static boolean migrate2to3(ExeRpCommands plugin, FileConfiguration cfg) {
        boolean changed = false;

        // radius.enable  ← radius-messages-enable
        if (!cfg.contains("radius.enable") && cfg.contains("radius-messages-enable")) {
            cfg.set("radius.enable", cfg.getBoolean("radius-messages-enable", true));
            changed = true;
            plugin.getLogger().info("[Config] Moved 'radius-messages-enable' → 'radius.enable'");
        }

        // permissions.enable ← permissions-enable
        if (!cfg.contains("permissions.enable") && cfg.contains("permissions-enable")) {
            cfg.set("permissions.enable", cfg.getBoolean("permissions-enable", false));
            changed = true;
            plugin.getLogger().info("[Config] Moved 'permissions-enable' → 'permissions.enable'");
        }

        // default distance (возьмём разумный дефолт из /me, если есть; иначе 50)
        int defaultDistance = 50;
        if (cfg.contains("commands.me.radius")) defaultDistance = cfg.getInt("commands.me.radius", 50);
        cfg.set("radius.default-distance", defaultDistance);
        changed = true;

        // commands.*.enable ← command-enable
        String[] cmds = new String[] { "me","do","try","roll","todo","whisper","shout" };
        for (String c : cmds) {
            String base = "commands." + c;
            if (cfg.contains(base + ".command-enable") && !cfg.contains(base + ".enable")) {
                cfg.set(base + ".enable", cfg.getBoolean(base + ".command-enable", true));
                changed = true;
                plugin.getLogger().info("[Config] Renamed '" + base + ".command-enable' → '" + base + ".enable'");
            }
            // убедимся, что permission ключ присутствует
            if (!cfg.contains(base + ".permission")) {
                cfg.set(base + ".permission", "");
                changed = true;
            }
            // radius у команды уже есть в v2 — оставляем как есть. Если нет — зададим дефолт
            if (!cfg.contains(base + ".radius")) {
                cfg.set(base + ".radius", defaultDistance);
                changed = true;
            }
        }

        // roll: allow-custom-range ← extended-enable
        if (cfg.contains("commands.roll.extended-enable") && !cfg.contains("commands.roll.allow-custom-range")) {
            cfg.set("commands.roll.allow-custom-range", cfg.getBoolean("commands.roll.extended-enable", true));
            changed = true;
            plugin.getLogger().info("[Config] Renamed 'commands.roll.extended-enable' → 'commands.roll.allow-custom-range'");
        }
        // default-min/max уже есть у тебя; если нет — подставим
        if (!cfg.contains("commands.roll.default-min-number")) {
            cfg.set("commands.roll.default-min-number", 0);
            changed = true;
        }
        if (!cfg.contains("commands.roll.default-max-number")) {
            cfg.set("commands.roll.default-max-number", 100);
            changed = true;
        }

        return changed;
    }

    private static void backupConfig(ExeRpCommands plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) return;
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File bak = new File(plugin.getDataFolder(), "config.yml.bak-" + stamp);
        try {
            java.nio.file.Files.copy(file.toPath(), bak.toPath());
            plugin.getLogger().info("[Config] Backup created: " + bak.getName());
        } catch (IOException e) {
            plugin.getLogger().warning("[Config] Backup failed: " + e.getMessage());
        }
    }
}
