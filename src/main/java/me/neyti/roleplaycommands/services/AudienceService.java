package me.neyti.roleplaycommands.services;

import me.neyti.roleplaycommands.RoleplayCommands;
import me.neyti.roleplaycommands.platform.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Рассылка сообщений адресатам.
 * Теперь НЕТ глобального свитча radius.enable – решает КАЖДАЯ команда:
 *   - global == true  -> всем онлайн игрокам;
 *   - global == false -> по радиусу в том же мире.
 * Реализация Folia-safe: рассылка на треде каждой сущности через EntityScheduler.
 */
public final class AudienceService {

    private final RoleplayCommands plugin;
    private final SchedulerAdapter scheduler;

    public AudienceService(RoleplayCommands plugin) {
        this.plugin = plugin;
        this.scheduler = new SchedulerAdapter(plugin);
    }

    public void sendToAudience(Player source, int radius, Consumer<Player> deliver) {
        sendToAudience(source, radius, false, deliver);
    }

    public void sendToAudience(Player source, int radius, boolean global, Consumer<Player> deliver) {
        if (global) {
            sendAll(deliver);
            return;
        }
        final int r = Math.max(0, radius);

        if (!scheduler.isFoliaLike()) {
            scheduler.runGlobal(() -> {
                Location src = source.getLocation();
                double r2 = (double) r * r;
                for (Player p : src.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(src) <= r2) {
                        deliver.accept(p);
                    }
                }
            });
            return;
        }

        Location snap = source.getLocation().clone();
        UUID worldId = snap.getWorld().getUID();
        double sx = snap.getX(), sy = snap.getY(), sz = snap.getZ();
        double r2 = (double) r * r;

        for (Player p : Bukkit.getOnlinePlayers()) {
            scheduler.runOnEntity(p, () -> {
                if (!p.getWorld().getUID().equals(worldId)) return;
                Location pl = p.getLocation();
                double dx = pl.getX() - sx, dy = pl.getY() - sy, dz = pl.getZ() - sz;
                if (dx*dx + dy*dy + dz*dz <= r2) {
                    deliver.accept(p);
                }
            }, null);
        }
    }

    public void sendAll(Consumer<Player> deliver) {
        if (!scheduler.isFoliaLike()) {
            scheduler.runGlobal(() -> {
                for (Player p : Bukkit.getOnlinePlayers()) deliver.accept(p);
            });
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            scheduler.runOnEntity(p, () -> deliver.accept(p), null);
        }
    }
}
