package me.neyti.roleplaycommands.managers;

import me.neyti.roleplaycommands.RoleplayCommands;
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

        cfg.setDefaults(defaults);
        cfg.options().copyDefaults(true);
    }
}
