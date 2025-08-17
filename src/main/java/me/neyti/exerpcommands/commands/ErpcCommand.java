package me.neyti.exerpcommands.commands;

import me.neyti.exerpcommands.ExeRpCommands;
import me.neyti.exerpcommands.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class ErpcCommand implements CommandExecutor, TabCompleter {

    private final ExeRpCommands plugin;

    public ErpcCommand(ExeRpCommands plugin) {
        this.plugin = plugin;
        // Регистрируем TabCompleter
        if (plugin.getCommand("erpc") != null) {
            plugin.getCommand("erpc").setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Обработка команды /erpc reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("exerpcommands.reload")) {
                String noPerm = plugin.getLanguageManager().getMessage("no_permission");
                ChatUtil.sendMessage(plugin, sender, noPerm);
                return true;
            }
            // Перезагрузка конфигурации
            plugin.getConfigManager().reload();
            plugin.getLanguageManager().reload();
            String reloadMsg = plugin.getLanguageManager().getMessage("reload");
            ChatUtil.sendMessage(plugin, sender, reloadMsg);
            return true;
        }

        // Если аргументы неверны, показываем подсказку
        ChatUtil.sendMessage(plugin, sender, "&cUsage: /erpc reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        // Подсказка для /erpc <Tab>
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
