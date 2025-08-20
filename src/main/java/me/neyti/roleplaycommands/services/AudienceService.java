package me.neyti.roleplaycommands.services;

import me.neyti.roleplaycommands.RoleplayCommands;
import me.neyti.roleplaycommands.platform.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

public final class AudienceService {

    private final RoleplayCommands plugin;
    private final SchedulerAdapter scheduler;

    public AudienceService(RoleplayCommands plugin) {
        this.plugin = plugin;
        SchedulerAdapter fromMain = null;
        try { fromMain = plugin.scheduler(); } catch (Throwable ignored) {}
        this.scheduler = (fromMain != null) ? fromMain : new SchedulerAdapter(plugin);
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

        scheduler.runOnEntity(source, () -> {
            Location snap = source.getLocation().clone();
            final UUID worldId = snap.getWorld().getUID();
            final double sx = snap.getX(), sy = snap.getY(), sz = snap.getZ();
            final double r2 = (double) r * r;

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
        }, null);
    }

    public void sendAll(Consumer<Player> deliver) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            scheduler.runOnEntity(p, () -> deliver.accept(p), null);
        }
    }
}
