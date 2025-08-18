package me.neyti.exerpcommands;

import me.neyti.exerpcommands.commands.*;
import me.neyti.exerpcommands.model.Messages;
import me.neyti.exerpcommands.listeners.CommandSendListener;
import me.neyti.exerpcommands.managers.ConfigManager;
import me.neyti.exerpcommands.managers.LanguageManager;
import me.neyti.exerpcommands.model.Settings;
import me.neyti.exerpcommands.services.AudienceService;
import me.neyti.exerpcommands.services.ChatService;
import me.neyti.exerpcommands.util.CommandRegistry;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class ExeRpCommands extends JavaPlugin {

    private ConfigManager configManager;
    private LanguageManager languageManager;

    private Settings settings;
    private Messages messages;

    private ChatService chatService;
    private AudienceService audienceService;
    private CommandRegistry commandRegistry;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.chatService = new ChatService(this);
        this.audienceService = new AudienceService(this);
        this.commandRegistry = new CommandRegistry(this);

        reloadEverything();

        // Регистрируем команды
        register("me", new MeCommand(this));
        register("do", new DoCommand(this));
        register("try", new TryCommand(this));
        register("roll", new RollCommand(this));
        register("todo", new TodoCommand(this));
        register("whisper", new WhisperCommand(this));
        register("w", new WhisperCommand(this));
        register("shout", new ShoutCommand(this));
        register("s", new ShoutCommand(this));

        // Новые:
        register("n", new NCommand(this));
        register("coin", new CoinCommand(this));
        register("dice", new DiceCommand(this));

        ErpcCommand erpc = new ErpcCommand(this);
        register("erpc", erpc, erpc);

        // Listener скрытия команд по настройкам (только если доступен класс события — 1.13+)
        try {
            Class.forName("org.bukkit.event.player.PlayerCommandSendEvent");
            getServer().getPluginManager().registerEvents(new CommandSendListener(this), this);
        } catch (Throwable ignored) {}

        // применяем видимость (раз-регистрация выключенных)
        applyCommandVisibility();

        try { new Metrics(this, 24427); } catch (Throwable ignored) {}
        getLogger().info(() -> "[" + getName() + "] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info(() -> "[" + getName() + "] Disabled");
    }

    public void reloadEverything() {
        configManager.reloadAndMigrate();
        this.settings = Settings.fromConfig(getConfig());

        languageManager.reload(settings);
        this.messages = languageManager.buildMessages(settings);

        applyCommandVisibility();
    }

    private void register(String name, Object executor) { register(name, executor, null); }
    private void register(String name, Object executor, Object tabCompleter) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Command '" + name + "' is not defined in plugin.yml");
            return;
        }
        if (executor instanceof org.bukkit.command.CommandExecutor) {
            cmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
        }
        if (tabCompleter instanceof org.bukkit.command.TabCompleter) {
            cmd.setTabCompleter((org.bukkit.command.TabCompleter) tabCompleter);
        }
    }

    private void applyCommandVisibility() {
        if (settings == null) return;
        Set<String> enabled = new HashSet<>();
        if (settings.commands.me.enable) enabled.add("me");
        if (settings.commands.doCmd.enable) enabled.add("do");
        if (settings.commands.tryCmd.enable) enabled.add("try");
        if (settings.commands.roll.enable) enabled.add("roll");
        if (settings.commands.todo.enable) enabled.add("todo");
        if (settings.commands.whisper.enable) { enabled.add("whisper"); enabled.add("w"); }
        if (settings.commands.shout.enable)   { enabled.add("shout");   enabled.add("s"); }
        // Новые:
        if (settings.commands.n.enable)       enabled.add("n");
        if (settings.commands.coin.enable)    enabled.add("coin");
        if (settings.commands.dice.enable)    enabled.add("dice");

        commandRegistry.applyVisibility(enabled);
    }

    public ConfigManager getConfigManager() { return configManager; }
    public LanguageManager getLanguageManager() { return languageManager; }

    public Settings getSettings() { return settings; }
    public Messages getMessages() { return messages; }

    public ChatService getChatService() { return chatService; }
    public AudienceService getAudienceService() { return audienceService; }
}
