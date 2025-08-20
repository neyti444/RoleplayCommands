package me.neyti.roleplaycommands.listeners;

import me.neyti.roleplaycommands.RoleplayCommands;
import me.neyti.roleplaycommands.model.Settings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public final class CommandSendListener implements Listener {

    private final RoleplayCommands plugin;

    public CommandSendListener(RoleplayCommands plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent e) {
        final Settings s = plugin.getSettings();
        if (s == null) return;

        final boolean permsOn = s.permissionsEnabled;

        java.util.function.BiConsumer<String, Settings.Command> filter = (label, conf) -> {
            if (!conf.enable) {
                e.getCommands().remove(label);
                return;
            }
            if (permsOn) {
                String node = (conf.permission != null && !conf.permission.trim().isEmpty())
                        ? conf.permission.trim()
                        : ("roleplaycommands." + conf.key);
                if (!e.getPlayer().hasPermission(node)) {
                    e.getCommands().remove(label);
                }
            }
        };

        filter.accept("me", s.commands.me);
        filter.accept("do", s.commands.doCmd);
        filter.accept("try", s.commands.tryCmd);
        filter.accept("roll", s.commands.roll);
        filter.accept("todo", s.commands.todo);
        filter.accept("whisper", s.commands.whisper);
        filter.accept("shout", s.commands.shout);
        filter.accept("n", s.commands.n);
        filter.accept("coin", s.commands.coin);
        filter.accept("dice", s.commands.dice);
    }
}
