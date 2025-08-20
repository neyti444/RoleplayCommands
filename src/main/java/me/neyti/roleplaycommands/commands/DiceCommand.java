package me.neyti.roleplaycommands.commands;

import me.neyti.roleplaycommands.RoleplayCommands;
import org.bukkit.entity.Player;

import java.util.Map;

public class DiceCommand extends AbstractMessageCommand {

    public DiceCommand(RoleplayCommands plugin) { super(plugin); }

    @Override protected String commandKey() { return "dice"; }

    @Override
    protected boolean executeFor(Player player, String[] args, int commandRadius, boolean global) {
        int rolled = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 7);

        Map<String, String> ph = phPlayer(player);
        ph.put("diceNumber", Integer.toString(rolled));

        plugin.getAudienceService().sendToAudience(player, commandRadius, global, p ->
                plugin.getChatService().sendTemplate(p, plugin.getMessages().DICE, ph, player)
        );
        return true;
    }
}

