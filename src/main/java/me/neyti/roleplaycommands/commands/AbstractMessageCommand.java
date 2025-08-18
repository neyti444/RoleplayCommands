package me.neyti.roleplaycommands.commands;

import me.neyti.roleplaycommands.RoleplayCommands;
import me.neyti.roleplaycommands.model.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMessageCommand implements CommandExecutor {

    protected final RoleplayCommands plugin;

    protected AbstractMessageCommand(RoleplayCommands plugin) {
        this.plugin = plugin;
    }

    protected abstract String commandKey();

    protected abstract boolean executeFor(Player player, String[] args, int commandRadius);

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
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

    protected static Map<String, String> phPlayer(Player p) {
        Map<String, String> ph = new HashMap<>(2);
        ph.put("player", p.getName());
        return ph;
    }
}
