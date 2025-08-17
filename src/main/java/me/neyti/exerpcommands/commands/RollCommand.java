package me.neyti.exerpcommands.commands;

import me.neyti.exerpcommands.ExeRpCommands;
import me.neyti.exerpcommands.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RollCommand implements CommandExecutor {

    private final ExeRpCommands plugin;

    public RollCommand(ExeRpCommands plugin) {
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
        if (!plugin.getConfigManager().isCommandEnabled("roll")) {
            ChatUtil.sendMessage(plugin, player, "&cThis command is disabled in the config.");
            return true;
        }

        // Проверка прав, если включено
        if (plugin.getConfigManager().isPermissionsEnabled()) {
            if (!player.hasPermission("exerpcommands.roll")) {
                String noPerm = plugin.getLanguageManager().getMessage("no_permission");
                ChatUtil.sendMessage(plugin, player, noPerm);
                return true;
            }
        }

        // Получаем настройки из config.yml
        int defaultMin = plugin.getConfigManager().getRollDefaultMin();
        int defaultMax = plugin.getConfigManager().getRollDefaultMax();
        boolean extendedEnable = plugin.getConfigManager().isRollExtended();

        int minRange = defaultMin;
        int maxRange = defaultMax;
        boolean isExtendedUsed = false;

        // Если включена расширенная функция и указаны оба параметра
        if (extendedEnable && args.length == 2) {
            try {
                minRange = Integer.parseInt(args[0]);
                maxRange = Integer.parseInt(args[1]);
                isExtendedUsed = true;
            } catch (NumberFormatException e) {
                String invNum = plugin.getLanguageManager().getMessage("invalid_number");
                ChatUtil.sendMessage(plugin, player, invNum);
                return true;
            }
            if (minRange > maxRange) {
                String invNum = plugin.getLanguageManager().getMessage("invalid_number");
                ChatUtil.sendMessage(plugin, player, invNum);
                return true;
            }
        }

        // Генерируем случайное число в диапазоне
        int rollNumber = (int) (Math.random() * (maxRange - minRange + 1)) + minRange;

        String formatKey = isExtendedUsed ? "roll.extended" : "roll.default";
        String format = plugin.getLanguageManager().getMessage(formatKey);
        format = format.replace("{player}", player.getName())
                .replace("{rollNumber}", String.valueOf(rollNumber))
                .replace("{defaultMinNumber}", String.valueOf(defaultMin))
                .replace("{defaultMaxNumber}", String.valueOf(defaultMax))
                .replace("{minNumber}", String.valueOf(minRange))
                .replace("{maxNumber}", String.valueOf(maxRange));

        // Отправка сообщения по радиусу или всем
        if (plugin.getConfigManager().isRadiusEnabled()) {
            int radius = plugin.getConfigManager().getCommandRadius("roll");
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
