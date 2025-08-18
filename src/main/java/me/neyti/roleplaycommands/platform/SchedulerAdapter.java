package me.neyti.roleplaycommands.platform;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Кросс-платформенный слой для Spigot/Paper/Folia/ShreddedPaper:
 * - В конструкторе детектим наличие Folia-API (Region/EntityScheduler) и биндим функции.
 * - Дальше никаких if — только вызовы преднастроенных раннеров.
 *
 * Документация Folia про типы планировщиков: Global/Region/Async/Entity. :contentReference[oaicite:1]{index=1}
 */
public final class SchedulerAdapter {

    @FunctionalInterface public interface GlobalRunner   { void run(Runnable task); }
    @FunctionalInterface public interface LocationRunner { void run(Location loc, Runnable task); }
    @FunctionalInterface public interface EntityRunner   { void run(Entity entity, Runnable task, Runnable retired); }

    private final Plugin plugin;

    private final boolean foliaLike;
    private final String serverBrand;

    private final GlobalRunner   globalRunner;
    private final LocationRunner locationRunner;
    private final EntityRunner   entityRunner;

    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.serverBrand = getBrand();
        boolean hasRegionScheduler  = isClassPresent("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
        boolean hasEntityScheduler  = isClassPresent("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
        this.foliaLike = hasRegionScheduler && hasEntityScheduler;

        if (!foliaLike) {
            this.globalRunner = task -> Bukkit.getScheduler().runTask(plugin, task);
            this.locationRunner = (loc, task) -> Bukkit.getScheduler().runTask(plugin, task);
            this.entityRunner = (entity, task, retired) -> Bukkit.getScheduler().runTask(plugin, task);
        } else {
            this.globalRunner = task -> {
                try {
                    Object grs = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                    grs.getClass().getMethod("execute", org.bukkit.plugin.Plugin.class, Runnable.class)
                            .invoke(grs, plugin, task);
                } catch (Throwable t) {
                    Bukkit.getScheduler().runTask(plugin, task);
                }
            };
            this.locationRunner = (loc, task) -> {
                try {
                    Object rs = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
                    rs.getClass().getMethod("execute", org.bukkit.plugin.Plugin.class, org.bukkit.Location.class, Runnable.class)
                            .invoke(rs, plugin, loc, task);
                } catch (Throwable t) {
                    globalRunner.run(task);
                }
            };
            this.entityRunner = (entity, task, retired) -> {
                try {
                    Object es = entity.getClass().getMethod("getScheduler").invoke(entity);
                    es.getClass().getMethod("run",
                                    org.bukkit.plugin.Plugin.class,
                                    java.util.function.Consumer.class,
                                    Runnable.class)
                            .invoke(es, plugin,
                                    (java.util.function.Consumer<Object>) scheduledTask -> task.run(),
                                    retired);
                } catch (Throwable t) {
                    globalRunner.run(task);
                }
            };
        }
    }

    public boolean isFoliaLike() { return foliaLike; }
    public String  getServerBrand() { return serverBrand; }

    public void runGlobal(Runnable task) { globalRunner.run(task); }
    public void runAt(Location loc, Runnable task) { locationRunner.run(loc, task); }
    public void runOnEntity(Entity entity, Runnable task, Runnable retired) { entityRunner.run(entity, task, retired); }

    private static boolean isClassPresent(String name) {
        try { Class.forName(name); return true; } catch (Throwable ignored) { return false; }
    }
    private static String getBrand() {
        try { return String.valueOf(Class.forName("org.bukkit.Bukkit").getMethod("getName").invoke(null)); }
        catch (Throwable t) { return "Unknown"; }
    }
}
