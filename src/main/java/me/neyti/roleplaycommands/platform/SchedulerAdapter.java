package me.neyti.roleplaycommands.platform;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class SchedulerAdapter {

    public interface Impl {
        void runGlobal(Runnable task);
        void runAsync(Runnable task);
        void runLater(long ticks, Runnable task);
        void runAt(Location loc, Runnable task);
        void runOnEntity(Entity entity, Runnable task, Runnable retired);
    }

    private final Plugin plugin;
    private final Impl impl;
    private final String serverBrand;

    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.serverBrand = detectBrand();
        this.impl = selectImpl(plugin, serverBrand);
    }

    public boolean isFoliaLike() { return impl instanceof FoliaImpl; }
    public String  getServerBrand() { return serverBrand; }

    public void runGlobal(Runnable task) { impl.runGlobal(task); }
    public void runAsync(Runnable task) { impl.runAsync(task); }
    public void runLater(long ticks, Runnable task) { impl.runLater(Math.max(0L, ticks), task); }
    public void runAt(Location loc, Runnable task) { impl.runAt(loc, task); }
    public void runOnEntity(Entity e, Runnable t, Runnable retired) { impl.runOnEntity(e, t, retired); }

    private static Impl selectImpl(Plugin plugin, String brand) {
        boolean foliaApi =
                hasClass("io.papermc.paper.threadedregions.RegionizedServer");
        boolean shredded = "ShreddedPaper".equalsIgnoreCase(brand);
        if (foliaApi || shredded) {
            try { return new FoliaImpl(plugin); }
            catch (Throwable t) {
                plugin.getLogger().warning("[SchedulerAdapter] Folia API not fully available, falling back to Bukkit impl: " + t);
            }
        }
        return new BukkitImpl(plugin);
    }

    private static boolean hasClass(String fqcn) {
        try { Class.forName(fqcn); return true; } catch (Throwable ignored) { return false; }
    }
    private static String detectBrand() {
        try {
            Method m = Class.forName("org.bukkit.Bukkit").getMethod("getName");
            return String.valueOf(m.invoke(null));
        } catch (Throwable t) { return "Unknown"; }
    }

    private static final class BukkitImpl implements Impl {
        private final Plugin plugin;
        BukkitImpl(Plugin plugin) { this.plugin = plugin; }

        @Override public void runGlobal(Runnable task)            { Bukkit.getScheduler().runTask(plugin, task); }
        @Override public void runAsync(Runnable task)             { Bukkit.getScheduler().runTaskAsynchronously(plugin, task); }
        @Override public void runLater(long ticks, Runnable task) { Bukkit.getScheduler().runTaskLater(plugin, task, ticks); }
        @Override public void runAt(Location loc, Runnable task)  { Bukkit.getScheduler().runTask(plugin, task); }
        @Override public void runOnEntity(Entity e, Runnable t, Runnable retired) { Bukkit.getScheduler().runTask(plugin, t); }
    }

    private static final class FoliaImpl implements Impl {
        private final Plugin plugin;

        private final Object       grsObj;
        private final MethodHandle grsExecute;
        private final MethodHandle grsRunDelayed;

        private final Object       rsObj;
        private final MethodHandle rsExecute;

        private final Object       asObj;
        private final MethodHandle asRunNow;

        FoliaImpl(Plugin plugin) throws Throwable {
            this.plugin = plugin;

            MethodHandles.Lookup L = MethodHandles.lookup();

            Method mGetGRS = Bukkit.class.getMethod("getGlobalRegionScheduler");
            grsObj = mGetGRS.invoke(null);
            grsExecute = L.unreflect(grsObj.getClass().getMethod("execute", Plugin.class, Runnable.class));

            Method mRunDelayed = null;
            try { mRunDelayed = grsObj.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class); }
            catch (NoSuchMethodException ignored) {}
            grsRunDelayed = (mRunDelayed != null) ? L.unreflect(mRunDelayed) : null;

            Method mGetRS = Bukkit.class.getMethod("getRegionScheduler");
            rsObj = mGetRS.invoke(null);
            rsExecute = L.unreflect(rsObj.getClass().getMethod("execute", Plugin.class, Location.class, Runnable.class));

            Object tmpAs = null; MethodHandle tmpRunNow = null;
            try {
                Method mGetAS = Bukkit.class.getMethod("getAsyncScheduler");
                tmpAs = mGetAS.invoke(null);
                tmpRunNow = L.unreflect(tmpAs.getClass().getMethod("runNow", Plugin.class, Consumer.class));
            } catch (Throwable ignored) {}
            asObj = tmpAs;
            asRunNow = tmpRunNow;
        }

        @Override public void runGlobal(Runnable task) {
            try { grsExecute.invoke(grsObj, plugin, task); }
            catch (Throwable t) { task.run(); }
        }

        @Override public void runAsync(Runnable task) {
            if (asObj != null && asRunNow != null) {
                try { asRunNow.invoke(asObj, plugin, (Consumer<Object>) st -> task.run()); return; }
                catch (Throwable ignored) {}
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }

        @Override public void runLater(long ticks, Runnable task) {
            if (grsRunDelayed != null) {
                try { grsRunDelayed.invoke(grsObj, plugin, (Consumer<Object>) st -> task.run(), ticks); return; }
                catch (Throwable ignored) {}
            }
            Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
        }

        @Override public void runAt(Location loc, Runnable task) {
            try { rsExecute.invoke(rsObj, plugin, loc, task); }
            catch (Throwable t) { runGlobal(task); }
        }

        @Override public void runOnEntity(Entity entity, Runnable task, Runnable retired) {
            try {
                Object es = entity.getClass().getMethod("getScheduler").invoke(entity);
                es.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class)
                        .invoke(es, plugin, (Consumer<Object>) st -> task.run(), retired);
            } catch (Throwable t) {
                runGlobal(task);
            }
        }
    }
}
