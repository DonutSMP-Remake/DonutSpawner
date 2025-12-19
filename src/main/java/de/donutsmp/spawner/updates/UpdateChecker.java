package de.donutsmp.spawner.updates;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import de.donutsmp.spawner.SpawnerPlugin;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final SpawnerPlugin plugin;
    private final String currentVersion;
    private final String apiUrl = "https://api.spigotmc.org/legacy/update.php?resource=XXXX"; // Replace XXXX with actual resource ID

    public UpdateChecker(SpawnerPlugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    public void checkForUpdates() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String latestVersion = reader.readLine();
                reader.close();

                if (latestVersion != null && !latestVersion.equals(currentVersion)) {
                    plugin.getLogger().info("A new version is available: " + latestVersion);
                } else {
                    plugin.getLogger().info("Plugin is up to date.");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }
}
