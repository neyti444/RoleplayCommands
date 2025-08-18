package me.neyti.exerpcommands.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Управляет видимостью/приоритетом команд:
 *  - выключенные полностью снимаем с карты команд;
 *  - включённые регистрируем/перерегистрируем так, чтобы именно НАШИ команды
 *    обрабатывали /me, /do и т.д. (перебиваем ванильные/чужие).
 * Работает на Spigot/Paper/Folia/ShreddedPaper (1.7+).
 */
public final class CommandRegistry {

    private final JavaPlugin plugin;

    private static final List<String> LABELS = Arrays.asList(
            "me", "do", "try", "roll", "todo", "whisper", "w", "shout", "s",
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

        // 1) Удалить ВЫКЛЮЧЕННЫЕ (оба ключа: короткий и namespaced)
        for (String label : LABELS) {
            if (!enabledLabels.contains(label)) {
                removeKey(known, label);
                removeKey(known, namespaced(label));
            }
        }

        // 2) ВКЛЮЧЕННЫЕ: обеспечить, что в коротком ключе стоит ИМЕННО наша команда
        String ns = plugin.getName().toLowerCase(Locale.ROOT);
        for (String label : LABELS) {
            if (!enabledLabels.contains(label)) continue;
            PluginCommand ours = originals.get(label);
            if (ours == null) continue;

            Command current = known.get(label);

            if (current != ours) {
                // если в коротком ключе чужая/ванильная — снимаем её и ставим нашу
                removeKey(known, label);
                removeKey(known, ns + ":" + label); // на всякий
                map.register(ns, ours);
            } else {
                // уже наша — ничего
            }
        }

        // 3) Синхронизировать дерево команд у сервера и клиентов
        syncCommandsTree();
        updatePlayersCommandTree();
    }

    /* ===================== helpers ===================== */

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

    private String namespaced(String label) {
        return plugin.getName().toLowerCase(Locale.ROOT) + ":" + label;
    }

    private void removeKey(Map<String, Command> known, String key) {
        if (key == null) return;
        Command cmd = known.remove(key);
        if (cmd == null) return;

        // удалить алиасы этой команды
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
            known.remove(namespaced(alias));
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
