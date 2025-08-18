package me.neyti.roleplaycommands.commands;

import me.neyti.roleplaycommands.RoleplayCommands;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CoinCommand extends AbstractMessageCommand {

    public CoinCommand(RoleplayCommands plugin) { super(plugin); }

    @Override protected String commandKey() { return "coin"; }

    @Override
    protected boolean executeFor(Player player, String[] args, int commandRadius) {
        boolean heads = ThreadLocalRandom.current().nextBoolean();

        Map<String, String> ph = phPlayer(player);
        ph.put("result", heads ? "heads" : "tails");

        final String template = heads ? plugin.getMessages().COIN_HEADS : plugin.getMessages().COIN_TAILS;

        boolean global = plugin.getSettings().commands.me.global;
        plugin.getAudienceService().sendToAudience(player, commandRadius, global,p ->
                plugin.getChatService().sendTemplate(p, template, ph, player)
        );
        return true;
    }
}
