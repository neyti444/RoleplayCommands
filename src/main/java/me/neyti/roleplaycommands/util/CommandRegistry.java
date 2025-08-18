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
            if (!enabledLabels.contains(label)) {
                removeKey(known, label);
                removeKey(known, namespaced(label));
            }
        }

        String ns = fallbackPrefix();
        for (String label : LABELS) {
            if (!enabledLabels.contains(label)) continue;
            PluginCommand ours = originals.get(label);
            if (ours == null) continue;

            Command current = known.get(label);
            if (current != ours) {
                removeKey(known, label);
                removeKey(known, ns + ":" + label);
                map.register(ns, ours);
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
                Map<String, Command> snapshot = new LinkedHashMap<>((Map<String, Command>) obj);
                f.set(map, snapshot);
                return snapshot;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private String fallbackPrefix() {
        String raw = plugin.getDescription().getName().toLowerCase(Locale.ROOT);
        return raw.replaceAll("[^a-z0-9_\\-]+", "");
    }

    private String namespaced(String label) {
        return fallbackPrefix() + ":" + label;
    }

    private void removeKey(Map<String, Command> known, String key) {
        if (key == null) return;
        Command cmd = known.remove(key);
        if (cmd == null) return;

        List<String> aliases = new ArrayList<>();
        try {
            Method getAliases = Command.class.getMethod("getAliases");
            Object res = getAliases.invoke(cmd);
            if (res instanceof List) {
                aliases.addAll(((List<?>) res).stream().map(String::valueOf).collect(Collectors.toList()));
            }
        } catch (Throwable ignored) {}

        for (String alias : aliases) {
            known.remove(alias);
            known.remove(fallbackPrefix() + ":" + alias);
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
