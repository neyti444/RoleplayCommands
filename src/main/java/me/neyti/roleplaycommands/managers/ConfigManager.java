package me.neyti.roleplaycommands.managers;

import me.neyti.roleplaycommands.RoleplayCommands;
import me.neyti.roleplaycommands.util.ConfigMigrator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final RoleplayCommands plugin;

    public ConfigManager(RoleplayCommands plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reloadAndMigrate() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource("config.yml"), StandardCharsets.UTF_8)
        );

        boolean migrated = ConfigMigrator.migrate(plugin, cfg, defaults);

        cfg.setDefaults(defaults);
        cfg.options().copyDefaults(true);

        int target = defaults.getInt("config-version", 3);
        if (cfg.getInt("config-version", 3) != target) {
            cfg.set("config-version", target);
            migrated = true;
        }

        if (migrated) plugin.getLogger().info("[Config] Migration applied. Saving updated config.yml");
        plugin.saveConfig();
    }
}
