package me.neyti.roleplaycommands.commands;

import me.neyti.roleplaycommands.RoleplayCommands;
import org.bukkit.entity.Player;

import java.util.Map;

public class TodoCommand extends AbstractMessageCommand {

    public TodoCommand(RoleplayCommands plugin) { super(plugin); }

    @Override protected String commandKey() { return "todo"; }

    @Override
    protected boolean executeFor(Player player, String[] args, int commandRadius) {
        if (args.length == 0) {
            plugin.getChatService().sendTemplate(player, plugin.getMessages().NO_ARGUMENTS, java.util.Collections.emptyMap(), player);
            return true;
        }

        String full = joinArgs(args);
        int idx = full.indexOf('*');
        if (idx <= 0 || idx >= full.length() - 1) {
            plugin.getChatService().sendTemplate(player, plugin.getMessages().NO_ARGUMENTS, java.util.Collections.emptyMap(), player);
            return true;
        }

        String m1 = full.substring(0, idx).trim();
        String m2 = full.substring(idx + 1).trim();

        Map<String, String> ph = phPlayer(player);
        ph.put("messageOne", m1);
        ph.put("messageTwo", m2);

        boolean global = plugin.getSettings().commands.me.global;
        plugin.getAudienceService().sendToAudience(player, commandRadius, global, p ->
                plugin.getChatService().sendTemplate(p, plugin.getMessages().TODO, ph, player)
        );
        return true;
    }
}
