package me.neyti.exerpcommands.commands;

import me.neyti.exerpcommands.ExeRpCommands;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RollCommand extends AbstractMessageCommand {

    public RollCommand(ExeRpCommands plugin) { super(plugin); }

    @Override protected String commandKey() { return "roll"; }

    @Override
    protected boolean executeFor(Player player, String[] args, int commandRadius) {
        final int defMin = plugin.getSettings().roll.defaultMin;
        final int defMax = plugin.getSettings().roll.defaultMax;

        int min = defMin, max = defMax;
        boolean extendedUsed = false;
        boolean global = plugin.getSettings().commands.me.global;

        if (plugin.getSettings().roll.allowCustomRange && args.length == 2) {
            // быстрая валидация без лишних объектов
            try {
                min = Integer.parseInt(args[0]);
                max = Integer.parseInt(args[1]);
                extendedUsed = true;
            } catch (NumberFormatException ex) {
                plugin.getChatService().sendTemplate(player, plugin.getMessages().INVALID_NUMBER, java.util.Collections.emptyMap(), player);
                return true;
            }
        }

        if (min > max) {
            plugin.getChatService().sendTemplate(player, plugin.getMessages().INVALID_NUMBER, java.util.Collections.emptyMap(), player);
            return true;
        }

        int rolled = ThreadLocalRandom.current().nextInt(min, max + 1);

        Map<String, String> ph = phPlayer(player);
        ph.put("rollNumber", Integer.toString(rolled));

        if (extendedUsed) {
            ph.put("minNumber", Integer.toString(min));
            ph.put("maxNumber", Integer.toString(max));
            plugin.getAudienceService().sendToAudience(player, commandRadius, global, p ->
                    plugin.getChatService().sendTemplate(p, plugin.getMessages().ROLL_EXTENDED, ph, player)
            );
        } else {
            ph.put("defaultMinNumber", Integer.toString(defMin));
            ph.put("defaultMaxNumber", Integer.toString(defMax));
            plugin.getAudienceService().sendToAudience(player, commandRadius, global, p ->
                    plugin.getChatService().sendTemplate(p, plugin.getMessages().ROLL_DEFAULT, ph, player)
            );
        }

        return true;
    }
}
