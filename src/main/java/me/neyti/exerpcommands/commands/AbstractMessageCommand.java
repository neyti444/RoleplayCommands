package me.neyti.exerpcommands.commands;

import me.neyti.exerpcommands.ExeRpCommands;
import me.neyti.exerpcommands.model.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Базовый класс команд.
 * Оптимизации:
 *  - нет стримов/regex в горячих путях;
 *  - быстрый join аргументов;
 *  - создаём маленькие Map только когда требуется.
 * Совместимость:
 *  - никаких вызовов, которых нет на старых Bukkit; весь Folia-специфик – в AudienceService.
 */
public abstract class AbstractMessageCommand implements CommandExecutor {

    protected final ExeRpCommands plugin;

    protected AbstractMessageCommand(ExeRpCommands plugin) {
        this.plugin = plugin;
    }

    /** Ключ команды в конфиге: "me", "do", "roll", ... */
    protected abstract String commandKey();

    /** Основная логика конкретной команды. */
    protected abstract boolean executeFor(Player player, String[] args, int commandRadius);

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // эта команда только для игроков
            plugin.getChatService().sendTemplate(sender, plugin.getMessages().PLAYERS_ONLY, Collections.emptyMap(), null);
            return true;
        }

        final Player player = (Player) sender;
        final Settings.Command conf = plugin.getSettings().commands.byKey(commandKey());

        if (!conf.enable) {
            plugin.getChatService().sendTemplate(player, plugin.getMessages().COMMAND_DISABLED, Collections.emptyMap(), player);
            return true;
        }

        if (plugin.getSettings().permissionsEnabled) {
            final String perm = conf.permission;
            if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) {
                Map<String, String> ph = new HashMap<>(2);
                ph.put("permission", perm);
                plugin.getChatService().sendTemplate(player, plugin.getMessages().NO_PERMISSION, ph, player);
                return true;
            }
        }

        return executeFor(player, args, conf.radius);
    }

    /** Быстрый join без промежуточных списков. */
    protected static String joinArgs(String[] args) {
        if (args.length == 1) return args[0];
        int len = 0;
        for (String a : args) len += a.length() + 1;
        StringBuilder sb = new StringBuilder(len > 0 ? len - 1 : 0);
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    /** Плейсхолдеры с игроком. */
    protected static Map<String, String> phPlayer(Player p) {
        Map<String, String> ph = new HashMap<>(2);
        ph.put("player", p.getName());
        return ph;
    }
}
