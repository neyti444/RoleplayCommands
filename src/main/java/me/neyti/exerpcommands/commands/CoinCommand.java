package me.neyti.exerpcommands.commands;

import me.neyti.exerpcommands.ExeRpCommands;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CoinCommand extends AbstractMessageCommand {

    public CoinCommand(ExeRpCommands plugin) { super(plugin); }

    @Override protected String commandKey() { return "coin"; }

    @Override
    protected boolean executeFor(Player player, String[] args, int commandRadius) {
        // /coin — без аргументов, подброс монеты
        boolean heads = ThreadLocalRandom.current().nextBoolean();

        Map<String, String> ph = phPlayer(player);
        // на всякий случай отдаём {result} в плейсхолдеры, если кому-то пригодится
        ph.put("result", heads ? "heads" : "tails");

        final String template = heads ? plugin.getMessages().COIN_HEADS : plugin.getMessages().COIN_TAILS;

        boolean global = plugin.getSettings().commands.me.global;
        plugin.getAudienceService().sendToAudience(player, commandRadius, global,p ->
                plugin.getChatService().sendTemplate(p, template, ph, player)
        );
        return true;
    }
}
