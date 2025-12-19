package de.donutsmp.spawner.nms;

import org.bukkit.Bukkit;
import de.donutsmp.spawner.SpawnerPlugin;

public class NMSManager {

    private final SpawnerPlugin plugin;
    private NMSHandler nmsHandler;

    public NMSManager(SpawnerPlugin plugin) {
        this.plugin = plugin;
        loadNMSHandler();
    }

    private void loadNMSHandler() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");
        String version = parts.length > 3 ? parts[3] : "default";
        try {
            Class<?> clazz = Class.forName("de.donutsmp.spawner.nms.NMSHandler_" + version);
            nmsHandler = (NMSHandler) clazz.getDeclaredConstructor().newInstance();
            plugin.getLogger().info("Loaded NMS handler for version " + version);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load NMS handler for version " + version + ": " + e.getMessage() + ". Using default handler.");
            nmsHandler = new DefaultNMSHandler();
        }
    }

    public NMSHandler getNMSHandler() {
        return nmsHandler;
    }
}
