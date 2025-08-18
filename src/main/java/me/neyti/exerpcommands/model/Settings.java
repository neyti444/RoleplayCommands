package me.neyti.exerpcommands.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class Settings {

    public final String languageCode;
    public final boolean hexColors;

    public final boolean permissionsEnabled;

    public final Commands commands;
    public final Roll roll;

    private Settings(String languageCode, boolean hexColors,
                     boolean permissionsEnabled,
                     Commands commands, Roll roll) {
        this.languageCode = languageCode;
        this.hexColors = hexColors;
        this.permissionsEnabled = permissionsEnabled;
        this.commands = commands;
        this.roll = roll;
    }

    public static Settings fromConfig(FileConfiguration cfg) {
        String lang = cfg.getString("language", "en");
        boolean hex = cfg.getBoolean("hex-colors-enable", true);

        boolean permEn = cfg.getBoolean("permissions.enable",
                cfg.getBoolean("permissions-enable", false));

        // Бэкомпат для старых конфигов (v2-v3):
        boolean legacyRadiusEnable = cfg.getBoolean("radius.enable",
                cfg.getBoolean("radius-messages-enable", true));
        int legacyDefaultRadius = cfg.getInt("radius.default-distance", 50);

        Commands cmds = new Commands(
                readCmd(cfg,"me", legacyRadiusEnable, legacyDefaultRadius),
                readCmd(cfg,"do", legacyRadiusEnable, legacyDefaultRadius),
                readCmd(cfg,"try", legacyRadiusEnable, legacyDefaultRadius),
                readCmd(cfg,"shout", legacyRadiusEnable, legacyDefaultRadius),
                readCmd(cfg,"whisper", legacyRadiusEnable, 5),
                readCmd(cfg,"todo", legacyRadiusEnable, legacyDefaultRadius),
                readCmd(cfg,"roll", legacyRadiusEnable, legacyDefaultRadius),
                readCmd(cfg,"n", legacyRadiusEnable, legacyDefaultRadius),
                readCmd(cfg,"coin", legacyRadiusEnable, legacyDefaultRadius),
                readCmd(cfg,"dice", legacyRadiusEnable, legacyDefaultRadius)
        );

        Roll roll = new Roll(
                cfg.getInt("commands.roll.default-min-number", 0),
                cfg.getInt("commands.roll.default-max-number", 100),
                cfg.getBoolean("commands.roll.allow-custom-range",
                        cfg.getBoolean("commands.roll.extended-enable", true))
        );

        return new Settings(lang, hex, permEn, cmds, roll);
    }

    private static Command readCmd(FileConfiguration cfg, String key,
                                   boolean legacyRadiusEnable, int legacyDefaultRadius) {
        String base = "commands." + key;
        boolean en = cfg.getBoolean(base + ".enable",
                cfg.getBoolean(base + ".command-enable", true));
        String perm = cfg.getString(base + ".permission", "");

        // Новая схема: либо radius (число), либо global (bool).
        boolean global = cfg.getBoolean(base + ".global", false);

        int radius;
        if (cfg.isInt(base + ".radius")) {
            radius = Math.max(0, cfg.getInt(base + ".radius"));
        } else if (!global) {
            // Бэкомпат: если старая секция radius.enable=false — трактуем как global=true
            if (!legacyRadiusEnable) {
                global = true;
                radius = 0;
            } else {
                // если radius.enable=true — берём legacy default-distance
                radius = legacyDefaultRadius;
            }
        } else {
            radius = 0;
        }

        return new Command(key, en, perm == null ? "" : perm.trim(), radius, global);
    }

    public static final class Commands {
        public final Command me, doCmd, tryCmd, shout, whisper, todo, roll;
        public final Command n, coin, dice;

        public Commands(Command me, Command doCmd, Command tryCmd, Command shout, Command whisper, Command todo, Command roll,
                        Command n, Command coin, Command dice) {
            this.me = me; this.doCmd = doCmd; this.tryCmd = tryCmd; this.shout = shout; this.whisper = whisper; this.todo = todo; this.roll = roll;
            this.n = n; this.coin = coin; this.dice = dice;
        }
        public Command byKey(String key) {
            switch (key) {
                case "me": return me;
                case "do": return doCmd;
                case "try": return tryCmd;
                case "shout": return shout;
                case "whisper": return whisper;
                case "todo": return todo;
                case "roll": return roll;
                case "n": return n;
                case "coin": return coin;
                case "dice": return dice;
                default: return new Command(key, true, "", 50, false);
            }
        }
    }

    public static final class Command {
        public final String key;
        public final boolean enable;
        public final String permission;
        public final int radius;       // если global=false
        public final boolean global;   // если true — игнорируем radius и шлём всем

        public Command(String key, boolean enable, String permission, int radius, boolean global) {
            this.key = key; this.enable = enable; this.permission = permission; this.radius = radius; this.global = global;
        }
    }

    public static final class Roll {
        public final int defaultMin;
        public final int defaultMax;
        public final boolean allowCustomRange;
        public Roll(int defaultMin, int defaultMax, boolean allowCustomRange) {
            this.defaultMin = defaultMin; this.defaultMax = defaultMax; this.allowCustomRange = allowCustomRange;
        }
    }
}
