package me.neyti.exerpcommands.commands;

import me.neyti.exerpcommands.ExeRpCommands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ErpcCommand implements CommandExecutor, TabCompleter {

    private final ExeRpCommands plugin;

    public ErpcCommand(ExeRpCommands plugin) { this.plugin = plugin; }

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
