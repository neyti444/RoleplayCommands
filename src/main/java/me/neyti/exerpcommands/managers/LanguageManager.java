package me.neyti.exerpcommands.managers;

import me.neyti.exerpcommands.ExeRpCommands;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {

    private final ExeRpCommands plugin;
    private Map<String, FileConfiguration> languages = new HashMap<>();
    private String selectedLanguage;

    private final String[] supportedLanguages = {"en", "ru"};

    public LanguageManager(ExeRpCommands plugin) {
        this.plugin = plugin;
        loadLanguages();
    }

    private void loadLanguages() {
        try {
            for (String lang : supportedLanguages) {
                File langFile = new File(plugin.getDataFolder(), lang + ".yml");
                if (!langFile.exists()) {
                    if (plugin.getResource(lang + ".yml") != null) {
                        plugin.saveResource(lang + ".yml", false);
                        Bukkit.getLogger().log(Level.INFO, ChatColor.GREEN + "Создан языковой файл: " + lang + ".yml");
                    } else {
                        Bukkit.getLogger().log(Level.SEVERE, ChatColor.RED + "Языковой файл " + lang + ".yml не найден в ресурсах!");
                        continue;
                    }
                }
                FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
                languages.put(lang, config);
                Bukkit.getLogger().log(Level.INFO, ChatColor.GREEN + "Загружен языковой файл: " + lang + ".yml");
            }
            selectedLanguage = plugin.getConfig().getString("language", "en");
            if (!languages.containsKey(selectedLanguage)) {
                Bukkit.getLogger().log(Level.WARNING, ChatColor.YELLOW + "Выбранный язык " + selectedLanguage + " не найден, используется 'en'.");
                selectedLanguage = "en";
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, ChatColor.RED + "Ошибка при загрузке языковых файлов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void reload() {
        loadLanguages();
        Bukkit.getLogger().log(Level.INFO, ChatColor.GREEN + "Языковая конфигурация перезагружена.");
    }

    public String getMessage(String key) {
        FileConfiguration langConfig = languages.get(selectedLanguage);
        if (langConfig == null) {
            return "&cMissing language configuration.";
        }
        return langConfig.getString("messages." + key, "&cMissing message: " + key);
    }

    // Опционально: метод для получения сообщений из определённого языка
    public String getMessage(String lang, String key) {
        FileConfiguration langConfig = languages.get(lang);
        if (langConfig == null) {
            return "&cMissing language configuration.";
        }
        return langConfig.getString("messages." + key, "&cMissing message: " + key);
    }
}
