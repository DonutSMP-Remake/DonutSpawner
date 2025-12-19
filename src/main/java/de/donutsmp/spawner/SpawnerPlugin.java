package de.donutsmp.spawner;

import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import de.donutsmp.spawner.listener.SpawnerListener;
import de.donutsmp.spawner.listener.InventoryListener;
import de.donutsmp.spawner.listener.BlockListener;
import de.donutsmp.spawner.manager.SpawnerManager;
import de.donutsmp.spawner.command.SpawnerCommand;
import de.donutsmp.spawner.config.ConfigManager;
import de.donutsmp.spawner.hologram.HologramManager;
import de.donutsmp.spawner.nms.NMSManager;
import de.donutsmp.spawner.scheduler.Scheduler;
import de.donutsmp.spawner.updates.UpdateChecker;

public class SpawnerPlugin extends JavaPlugin {

    private static SpawnerPlugin instance;
    private SpawnerManager spawnerManager;
    private ConfigManager configManager;
    private HologramManager hologramManager;
    private NMSManager nmsManager;
    private Scheduler scheduler;
    private UpdateChecker updateChecker;
    private Metrics metrics;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        hologramManager = new HologramManager();
        scheduler = new Scheduler(this);
        spawnerManager = new SpawnerManager(this);
        nmsManager = new NMSManager(this);
        updateChecker = new UpdateChecker(this);

        metrics = new Metrics(this, 12345);

        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(spawnerManager), this);
        getServer().getPluginManager().registerEvents(new BlockListener(spawnerManager), this);

        getCommand("spawner").setExecutor(new SpawnerCommand(this));
        getCommand("spawnergive").setExecutor(new SpawnerCommand(this));

        updateChecker.checkForUpdates();

        getLogger().info("DonutSMP Spawner Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        getLogger().info("DonutSMP Spawner Plugin disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public static SpawnerPlugin getInstance() {
        return instance;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public NMSManager getNMSManager() {
        return nmsManager;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
