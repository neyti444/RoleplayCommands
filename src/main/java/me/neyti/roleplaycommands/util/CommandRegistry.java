package me.neyti.roleplaycommands.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandRegistry {

    private final JavaPlugin plugin;

    private static final List<String> LABELS = Arrays.asList(
            "me", "do", "try", "roll", "todo", "whisper", "shout",
            "n", "coin", "dice"
    );

    private final Map<String, PluginCommand> originals = new HashMap<>();

    public CommandRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        for (String label : LABELS) {
            PluginCommand pc = plugin.getCommand(label);
            if (pc != null) originals.put(label, pc);
        }
    }

    public void applyVisibility(Set<String> enabledLabels) {
        SimpleCommandMap map = getCommandMap();
        if (map == null) return;

        Map<String, Command> known = getKnownCommands(map);
        if (known == null) return;

        for (String label : LABELS) {
            if (enabledLabels.contains(label)) continue;
            PluginCommand ours = originals.get(label);
            if (ours == null) continue;

            removeIfPointsTo(known, label,    ours);
            removeIfPointsTo(known, ns(label), ours);

            for (String alias : safeAliases(ours)) {
                removeIfPointsTo(known, alias,      ours);
                removeIfPointsTo(known, ns(alias),  ours);
            }
        }

        String prefix = fallbackPrefix();
        for (String label : LABELS) {
            if (!enabledLabels.contains(label)) continue;
            PluginCommand ours = originals.get(label);
            if (ours == null) continue;

            Command current = known.get(label);
            if (current != ours) {
                known.remove(label);
                removeIfPointsTo(known, prefix + ":" + label, ours);

                map.register(prefix, ours);
            }
        }

        syncCommandsTree();
        updatePlayersCommandTree();
    }

    private SimpleCommandMap getCommandMap() {
        try {
            Method m = Bukkit.getServer().getClass().getMethod("getCommandMap");
            Object cmdMap = m.invoke(Bukkit.getServer());
            if (cmdMap instanceof SimpleCommandMap) return (SimpleCommandMap) cmdMap;
        } catch (Throwable ignored) {}
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> getKnownCommands(SimpleCommandMap map) {
        try {
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Object obj = f.get(map);
            if (obj instanceof Map) {
                return (Map<String, Command>) obj;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private String fallbackPrefix() {
        String raw = plugin.getDescription().getName().toLowerCase(Locale.ROOT);
        return raw.replaceAll("[^a-z0-9_\\-]+", "");
    }

    private String ns(String label) {
        return fallbackPrefix() + ":" + label;
    }

    private static List<String> safeAliases(Command cmd) {
        try {
            List<String> a = cmd.getAliases();
            if (a == null || a.isEmpty()) return Collections.emptyList();
            return a.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    private static void removeIfPointsTo(Map<String, Command> known, String key, Command target) {
        if (key == null) return;
        Command current = known.get(key);
        if (current == target) {
            known.remove(key);
        }
    }

    private void syncCommandsTree() {
        try {
            Method m = Bukkit.getServer().getClass().getMethod("syncCommands");
            m.invoke(Bukkit.getServer());
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable ignored) {}
    }

    private void updatePlayersCommandTree() {
        try {
            Method updater = Class.forName("org.bukkit.entity.Player").getMethod("updateCommands");
            Bukkit.getOnlinePlayers().forEach(p -> {
                try { updater.invoke(p); } catch (Throwable ignored) {}
            });
        } catch (Throwable ignored) {}
    }
}
