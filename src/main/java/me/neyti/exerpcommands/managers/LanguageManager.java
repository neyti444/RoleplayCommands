package me.neyti.exerpcommands.managers;

import me.neyti.exerpcommands.ExeRpCommands;
import me.neyti.exerpcommands.model.Messages;
import me.neyti.exerpcommands.model.Settings;
import me.neyti.exerpcommands.util.Formatting;
import me.neyti.exerpcommands.util.LanguageMigrator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LanguageManager {

    private final ExeRpCommands plugin;
    private FileConfiguration langCfg;
    private String activeLanguage = "en";

    public LanguageManager(ExeRpCommands plugin) {
        this.plugin = plugin;
        ensureLanguageFiles();
    }

    public void reload(Settings settings) {
        ensureLanguageFiles();

        String code = settings.languageCode == null ? "en" : settings.languageCode.trim();

        LanguageMigrator.migrate(plugin, "ru");
        LanguageMigrator.migrate(plugin, "en");
        if (!"ru".equalsIgnoreCase(code) && !"en".equalsIgnoreCase(code)) {
            LanguageMigrator.migrate(plugin, code);
        }

        File langFile = new File(plugin.getDataFolder(), code + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("[Lang] File " + code + ".yml not found. Falling back to en.yml");
            code = "en";
            langFile = new File(plugin.getDataFolder(), "en.yml");
        }
        this.activeLanguage = code;
        this.langCfg = YamlConfiguration.loadConfiguration(langFile);

        plugin.getLogger().info("[Lang] Loaded language: " + code + " (" + langFile.getName() + ")");
    }

    public Messages buildMessages(Settings settings) {
        String PLAYERS_ONLY     = color(get("messages.players_only", "&cPlayers only."), settings);
        String COMMAND_DISABLED = color(get("messages.command_disabled", "&cThis command is disabled in the config."), settings);
        String NO_PERMISSION    = color(get("messages.no_permission", "&cYou don't have permission &7({permission})&c."), settings);
        String NO_ARGUMENTS     = color(get("messages.no_arguments", "&cYou must provide arguments."), settings);
        String INVALID_NUMBER   = color(get("messages.invalid_number", "&cInvalid number or range."), settings);
        String RELOADED         = color(get("messages.reloaded", "&aConfiguration and languages reloaded."), settings);
        String USAGE_ERPC       = color(get("messages.usage_erpc", "&eUsage: /erpc reload"), settings);

        String ME      = color(get("messages.me", "&d{player} {message}"), settings);
        String DO      = color(get("messages.do", "&3{message} ({player})"), settings);
        String WHISPER = color(get("messages.whisper", "&7{player} whispers: {message}"), settings);
        String SHOUT   = color(get("messages.shout", "&e{player} shouts: {message}"), settings);
        String TODO    = color(get("messages.todo", "{messageOne} - said {player}, {messageTwo}"), settings);
        String N       = color(get("messages.n", "&7{player}: {message}"), settings);

        String TRY_SUCCESS = color(get("messages.try.success", "{player} {message} &a(success)"), settings);
        String TRY_FAILURE = color(get("messages.try.failure", "{player} {message} &c(failure)"), settings);

        String ROLL_DEFAULT  = color(get("messages.roll.default", "&7(({player} rolled {rollNumber}. [ {defaultMinNumber} – {defaultMaxNumber} ]))"), settings);
        String ROLL_EXTENDED = color(get("messages.roll.extended","&7(({player} rolled {rollNumber}. [ {minNumber} – {maxNumber} ]))"), settings);

        String COIN_HEADS = color(get("messages.coin.heads", "&7{player} flipped a coin: &aHeads"), settings);
        String COIN_TAILS = color(get("messages.coin.tails", "&7{player} flipped a coin: &cTails"), settings);

        String DICE = color(get("messages.dice", "&7{player} rolled a dice: &e{diceNumber}"), settings);

        return new Messages(
                PLAYERS_ONLY, COMMAND_DISABLED, NO_PERMISSION, NO_ARGUMENTS, INVALID_NUMBER,
                RELOADED, USAGE_ERPC,
                ME, DO, WHISPER, SHOUT, TODO, N,
                TRY_SUCCESS, TRY_FAILURE,
                ROLL_DEFAULT, ROLL_EXTENDED,
                COIN_HEADS, COIN_TAILS,
                DICE
        );
    }

    public String getActiveLanguage() { return activeLanguage; }

    private String get(String path, String def) {
        return (langCfg != null) ? langCfg.getString(path, def) : def;
    }

    private String color(String s, Settings settings) {
        return Formatting.applyAllOnce(s, settings.hexColors);
    }

    private void ensureLanguageFiles() {
        saveIfMissing("en.yml");
        saveIfMissing("ru.yml");
    }

    private void saveIfMissing(String name) {
        File f = new File(plugin.getDataFolder(), name);
        if (!f.exists() && plugin.getResource(name) != null) {
            plugin.saveResource(name, false);
        }
    }
}
