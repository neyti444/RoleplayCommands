package me.neyti.exerpcommands.commands;

import me.neyti.exerpcommands.ExeRpCommands;
import me.neyti.exerpcommands.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TodoCommand implements CommandExecutor {

    private final ExeRpCommands plugin;

    public TodoCommand(ExeRpCommands plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            ChatUtil.sendMessage(plugin, sender, "Players only.");
            return true;
        }
        Player player = (Player) sender;

        // Проверка включенности команды
        if (!plugin.getConfigManager().isCommandEnabled("todo")) {
            ChatUtil.sendMessage(plugin, player, "&cThis command is disabled in the config.");
            return true;
        }

        // Проверка прав, если включено
        if (plugin.getConfigManager().isPermissionsEnabled()) {
            if (!player.hasPermission("exerpcommands.todo")) {
                String noPerm = plugin.getLanguageManager().getMessage("no_permission");
                ChatUtil.sendMessage(plugin, player, noPerm);
                return true;
            }
        }

        // Проверка наличия аргументов и символа '*'
        if (args.length == 0) {
            String noArgs = plugin.getLanguageManager().getMessage("no_arguments");
            ChatUtil.sendMessage(plugin, player, noArgs);
            return true;
        }

        String fullMessage = String.join(" ", args);
        if (!fullMessage.contains("*")) {
            String noArgs = plugin.getLanguageManager().getMessage("no_arguments");
            ChatUtil.sendMessage(plugin, player, noArgs);
            return true;
        }

        // Разделяем сообщение по первому '*'
        String[] split = fullMessage.split("\\*", 2);
        if (split.length < 2) {
            String noArgs = plugin.getLanguageManager().getMessage("no_arguments");
            ChatUtil.sendMessage(plugin, player, noArgs);
            return true;
        }

        String messageOne = split[0].trim();
        String messageTwo = split[1].trim();

        String format = plugin.getLanguageManager().getMessage("todo");
        format = format.replace("{player}", player.getName())
                .replace("{messageOne}", messageOne)
                .replace("{messageTwo}", messageTwo);

        // Отправка сообщения по радиусу или всем
        if (plugin.getConfigManager().isRadiusEnabled()) {
            int radius = plugin.getConfigManager().getCommandRadius("todo");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(player.getWorld())
                        && p.getLocation().distance(player.getLocation()) <= radius) {
                    ChatUtil.sendMessage(plugin, p, format);
                }
            }
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                ChatUtil.sendMessage(plugin, p, format);
            }
        }
        return true;
    }
}
