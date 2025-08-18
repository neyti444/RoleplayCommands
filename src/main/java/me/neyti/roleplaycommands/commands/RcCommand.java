package me.neyti.roleplaycommands.commands;

import me.neyti.roleplaycommands.RoleplayCommands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RcCommand implements CommandExecutor, TabCompleter {

    private final RoleplayCommands plugin;

    public RcCommand(RoleplayCommands plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadEverything();
            plugin.getChatService().sendTemplate(sender, plugin.getMessages().RELOADED, java.util.Collections.emptyMap(), null);
            return true;
        }
        plugin.getChatService().sendTemplate(sender, plugin.getMessages().USAGE_ERPC, java.util.Collections.emptyMap(), null);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("reload");
        return Collections.emptyList();
    }
}
