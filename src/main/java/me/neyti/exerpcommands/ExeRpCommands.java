package me.neyti.exerpcommands;

import me.neyti.exerpcommands.commands.*;
import me.neyti.exerpcommands.managers.ConfigManager;
import me.neyti.exerpcommands.managers.LanguageManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics; // Импорт bStats

public class ExeRpCommands extends JavaPlugin {

    private ConfigManager configManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        // Инициализация менеджера конфигурации
        configManager = new ConfigManager(this);

        // Инициализация менеджера языков
        languageManager = new LanguageManager(this);

        // Регистрация команд
        registerCommands();

        // Инициализация bStats
        int pluginId = 24427;
        try {
            Metrics metrics = new Metrics(this, pluginId);
            getLogger().info("[bStats] Metrics initialized with Plugin ID: " + pluginId);
        } catch (Exception e) {
            getLogger().severe("[bStats] Failed to initialize Metrics: " + e.getMessage());
            e.printStackTrace();
        }

        // Логирование успешной загрузки
        getLogger().info("ExeRpCommands successfully loaded!");
    }

    @Override
    public void onDisable() {
        // Действия при отключении плагина (если необходимо)
    }

    private void registerCommands() {
        getCommand("me").setExecutor(new MeCommand(this));
        getCommand("do").setExecutor(new DoCommand(this));
        getCommand("try").setExecutor(new TryCommand(this));
        getCommand("roll").setExecutor(new RollCommand(this));
        getCommand("todo").setExecutor(new TodoCommand(this));
        getCommand("whisper").setExecutor(new WhisperCommand(this));
        getCommand("w").setExecutor(new WhisperCommand(this)); // Алиас
        getCommand("shout").setExecutor(new ShoutCommand(this));
        getCommand("s").setExecutor(new ShoutCommand(this)); // Алиас
        getCommand("erpc").setExecutor(new ErpcCommand(this));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
