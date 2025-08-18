package me.neyti.roleplaycommands.commands;

import me.neyti.roleplaycommands.RoleplayCommands;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DiceCommand extends AbstractMessageCommand {

    public DiceCommand(RoleplayCommands plugin) { super(plugin); }

    @Override protected String commandKey() { return "dice"; }

    @Override
    protected boolean executeFor(Player player, String[] args, int commandRadius) {
        int rolled = ThreadLocalRandom.current().nextInt(1, 7);

        Map<String, String> ph = phPlayer(player);
        ph.put("diceNumber", Integer.toString(rolled));

        boolean global = plugin.getSettings().commands.me.global;
        plugin.getAudienceService().sendToAudience(player, commandRadius, global, p ->
                plugin.getChatService().sendTemplate(p, plugin.getMessages().DICE, ph, player)
        );
        return true;
    }
}
