package me.neyti.exerpcommands.managers;

import me.neyti.exerpcommands.ExeRpCommands;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final ExeRpCommands plugin;

    public ConfigManager(ExeRpCommands plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig(); // Копирует config.yml из ресурсов, если он отсутствует
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public boolean isCommandEnabled(String command) {
        return plugin.getConfig().getBoolean("commands." + command + ".command-enable", false);
    }

    public boolean isPermissionsEnabled() {
        return plugin.getConfig().getBoolean("permissions-enable", false);
    }

    public boolean isRadiusEnabled() {
        return plugin.getConfig().getBoolean("radius-messages-enable", false);
    }

    public int getCommandRadius(String command) {
        return plugin.getConfig().getInt("commands." + command + ".radius", 100);
    }

    public boolean isRollExtended() {
        return plugin.getConfig().getBoolean("commands.roll.extended-enable", false);
    }

    public int getRollDefaultMin() {
        return plugin.getConfig().getInt("commands.roll.default-min-number", 1);
    }

    public int getRollDefaultMax() {
        return plugin.getConfig().getInt("commands.roll.default-max-number", 100);
    }

    public boolean isHexEnabled() {
        return plugin.getConfig().getBoolean("hex-colors-enable", false);
    }
}
