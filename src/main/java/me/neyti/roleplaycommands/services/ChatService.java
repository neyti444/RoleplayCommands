package me.neyti.roleplaycommands.services;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neyti.roleplaycommands.RoleplayCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class ChatService {

    private final RoleplayCommands plugin;

    public ChatService(RoleplayCommands plugin) {
        this.plugin = plugin;
    }

    public void sendTemplate(CommandSender target, String precoloredTemplate, Map<String, String> placeholders, Player papiContext) {
        String msg = fill(precoloredTemplate, placeholders, papiContext);
        target.sendMessage(msg);
    }

    public String fill(String precoloredTemplate, Map<String, String> placeholders, Player papiContext) {
        String out = precoloredTemplate;
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            out = PlaceholderAPI.setPlaceholders(papiContext, out);
        }
        return out;
    }
}
