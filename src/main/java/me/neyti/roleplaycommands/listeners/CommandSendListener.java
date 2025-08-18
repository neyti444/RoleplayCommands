package me.neyti.roleplaycommands.listeners;

import me.neyti.roleplaycommands.RoleplayCommands;
import me.neyti.roleplaycommands.model.Settings;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Collection;
import java.util.Locale;

public final class CommandSendListener implements Listener {

    private final RoleplayCommands plugin;

    public CommandSendListener(RoleplayCommands plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent e) {
        final Settings s = plugin.getSettings();
        if (s == null) return;

        final String ns = Bukkit.getName().toLowerCase(Locale.ROOT) + ":";

        maybeRemove(e.getCommands(), "me",      s.commands.me.enable,        ns);
        maybeRemove(e.getCommands(), "do",      s.commands.doCmd.enable,     ns);
        maybeRemove(e.getCommands(), "try",     s.commands.tryCmd.enable,    ns);
        maybeRemove(e.getCommands(), "roll",    s.commands.roll.enable,      ns);
        maybeRemove(e.getCommands(), "todo",    s.commands.todo.enable,      ns);
        maybeRemove(e.getCommands(), "whisper", s.commands.whisper.enable,   ns);
        maybeRemove(e.getCommands(), "shout",   s.commands.shout.enable,     ns);
        maybeRemove(e.getCommands(), "n",       s.commands.n.enable,         ns);
        maybeRemove(e.getCommands(), "coin",    s.commands.coin.enable,      ns);
        maybeRemove(e.getCommands(), "dice",    s.commands.dice.enable,      ns);
    }

    private static void maybeRemove(Collection<String> cmds, String label, boolean enabled, String ns) {
        if (enabled) return;
        cmds.remove(label);
        cmds.remove(ns + label);
    }
}
