package me.neyti.roleplaycommands.commands;

import me.neyti.roleplaycommands.RoleplayCommands;
import org.bukkit.entity.Player;

import java.util.Map;

public class WhisperCommand extends AbstractMessageCommand {

    public WhisperCommand(RoleplayCommands plugin) { super(plugin); }

    @Override protected String commandKey() { return "whisper"; }

    @Override
    protected boolean executeFor(Player player, String[] args, int commandRadius, boolean global) {
        if (args.length == 0) {
            plugin.getChatService().sendTemplate(player, plugin.getMessages().NO_ARGUMENTS, java.util.Collections.emptyMap(), player);
            return true;
        }

        String message = joinArgs(args);
        Map<String, String> ph = phPlayer(player);
        ph.put("message", message);

        plugin.getAudienceService().sendToAudience(player, commandRadius, global, p ->
                plugin.getChatService().sendTemplate(p, plugin.getMessages().WHISPER, ph, player)
        );
        return true;
    }
}
