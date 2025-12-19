package de.donutsmp.spawner.scheduler;

import org.bukkit.scheduler.BukkitRunnable;
import de.donutsmp.spawner.SpawnerPlugin;

public class Scheduler {

    private final SpawnerPlugin plugin;

    public Scheduler(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }

    public void runTaskLater(Runnable task, long delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskLater(plugin, delay);
    }

    public void runTaskTimer(Runnable task, long delay, long period) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskTimer(plugin, delay, period);
    }

    public void runTaskAsync(Runnable task) {
        new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskAsynchronously(plugin);
    }
}
