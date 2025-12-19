package de.donutsmp.spawner.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import de.donutsmp.spawner.SpawnerPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final SpawnerPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(SpawnerPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public int getDelayIncrement() {
        return config.getInt("delay-increment", 100);
    }

    public int getMaxStackSize() {
        return config.getInt("max-stack-size", 64);
    }

    public boolean isStackingEnabled() {
        return config.getBoolean("stacking-enabled", true);
    }

    public boolean allowBreakExistingSpawners() {
        return config.getBoolean("allow-break-existing-spawners", true);
    }

    public boolean requireSilkTouch() {
        return config.getBoolean("require-silk-touch", false);
    }

    public int getMinDelay() {
        return config.getInt("min-delay", 1);
    }

    public int getMaxDelay() {
        return config.getInt("max-delay", 600);
    }
}
