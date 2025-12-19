package de.donutsmp.spawner.manager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.donutsmp.spawner.SpawnerPlugin;
import de.donutsmp.spawner.config.ConfigManager;
import de.donutsmp.spawner.hologram.HologramManager;

public class SpawnerManager {

    private final SpawnerPlugin plugin;
    private final ConfigManager config;
    private final HologramManager hologram;

    private static final NamespacedKey STACK_KEY = new NamespacedKey("donutsmp", "stack_size");
    private static final NamespacedKey DELAY_KEY = new NamespacedKey("donutsmp", "custom_delay");

    private final Map<EntityType, List<Material>> dropsMap = new HashMap<>();
    private final Random random = new Random();
    private final Map<String, FileConfiguration> spawnerData = new HashMap<>();
    private final Map<String, File> spawnerFiles = new HashMap<>();

    public SpawnerManager(SpawnerPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.hologram = plugin.getHologramManager();

        dropsMap.put(EntityType.ZOMBIE, List.of(Material.ROTTEN_FLESH));
        dropsMap.put(EntityType.SKELETON, List.of(Material.BONE, Material.ARROW));
        dropsMap.put(EntityType.SPIDER, List.of(Material.STRING, Material.SPIDER_EYE));
        dropsMap.put(EntityType.CAVE_SPIDER, List.of(Material.STRING, Material.SPIDER_EYE));
        dropsMap.put(EntityType.CREEPER, List.of(Material.GUNPOWDER));
        dropsMap.put(EntityType.ENDERMAN, List.of(Material.ENDER_PEARL));
        dropsMap.put(EntityType.BLAZE, List.of(Material.BLAZE_ROD));

        startVirtualSpawning();
    }

    private void startVirtualSpawning() {
        plugin.getScheduler().runTaskTimer(() -> {
            for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                    for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
                        if (state instanceof CreatureSpawner) {
                            Block block = state.getBlock();
                            int currentStack = getStackCount(block);
                            if (currentStack < config.getMaxStackSize()) {
                                setStackCount(block, currentStack + 1);
                            }
                        }
                    }
                }
            }
        }, 20 * 60, 20 * 60);
    }

    private String getSpawnerKey(Block block) {
        return block.getWorld().getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ();
    }

    private File getSpawnerFile(Block block) {
        String key = getSpawnerKey(block);
        if (!spawnerFiles.containsKey(key)) {
            File dataFolder = new File(plugin.getDataFolder(), "spawners");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File file = new File(dataFolder, key + ".yml");
            spawnerFiles.put(key, file);
        }
        return spawnerFiles.get(key);
    }

    private FileConfiguration getSpawnerConfig(Block block) {
        String key = getSpawnerKey(block);
        if (!spawnerData.containsKey(key)) {
            File file = getSpawnerFile(block);
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            spawnerData.put(key, config);
        }
        return spawnerData.get(key);
    }

    private void saveSpawnerConfig(Block block) {
        String key = getSpawnerKey(block);
        FileConfiguration config = spawnerData.get(key);
        if (config != null) {
            try {
                config.save(getSpawnerFile(block));
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save spawner data for " + key);
            }
        }
    }

    public void unloadSpawnerData(Block block) {
        String key = getSpawnerKey(block);
        if (spawnerData.containsKey(key)) {
            saveSpawnerConfig(block);
            spawnerData.remove(key);
            spawnerFiles.remove(key);
        }
    }

    public int getStackCount(Block block) {
        if (!(block.getState() instanceof CreatureSpawner)) {
            return 0;
        }
        FileConfiguration spawnerConfig = getSpawnerConfig(block);
        if (spawnerConfig.contains("stack")) {
            return spawnerConfig.getInt("stack");
        }
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        Integer stack = spawner.getPersistentDataContainer().get(STACK_KEY, PersistentDataType.INTEGER);
        return stack != null ? stack : 1;
    }

    public void setStackCount(Block block, int count) {
        if (!(block.getState() instanceof CreatureSpawner) || count < 1) {
            return;
        }
        if (!config.isStackingEnabled()) {
            count = 1;
        } else if (count > config.getMaxStackSize()) {
            count = config.getMaxStackSize();
        }
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        spawner.getPersistentDataContainer().set(STACK_KEY, PersistentDataType.INTEGER, count);
        spawner.update();
        updateHologram(block);
        adjustDelay(block);
    }

    public boolean addToStack(Block block) {
        int current = getStackCount(block);
        if (current >= config.getMaxStackSize() || !config.isStackingEnabled()) {
            return false;
        }
        setStackCount(block, current + 1);
        return true;
    }

    public boolean removeFromStack(Block block) {
        int current = getStackCount(block);
        if (current <= 1) {
            return false;
        }
        setStackCount(block, current - 1);
        return true;
    }

    public int getDelay(Block block) {
        if (!(block.getState() instanceof CreatureSpawner)) {
            return 200;
        }
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        Integer delay = spawner.getPersistentDataContainer().get(DELAY_KEY, PersistentDataType.INTEGER);
        return delay != null ? delay : spawner.getDelay();
    }

    public void setDelay(Block block, int delay) {
        if (!(block.getState() instanceof CreatureSpawner)) {
            return;
        }
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        int minDelay = config.getMinDelay();
        int maxDelay = config.getMaxDelay();
        delay = Math.max(minDelay, Math.min(maxDelay, delay));
        spawner.getPersistentDataContainer().set(DELAY_KEY, PersistentDataType.INTEGER, delay);
        spawner.setDelay(delay);
        spawner.update();
    }

    private void adjustDelay(Block block) {
        int stack = getStackCount(block);
        if (stack > 1) {
            int baseDelay = 200;
            int adjusted = Math.max(1, baseDelay / stack);
            setDelay(block, adjusted);
        }
    }

    public void updateHologram(Block block) {
        int stack = getStackCount(block);
        if (stack > 0) {
            hologram.updateHologram(block, stack);
        } else {
            hologram.removeHologram(block);
        }
    }

    public EntityType getSpawnerType(Block block) {
        if (!(block.getState() instanceof CreatureSpawner)) {
            return null;
        }
        return ((CreatureSpawner) block.getState()).getSpawnedType();
    }

    public List<ItemStack> getVirtualDrops(EntityType type, int stackSize) {
        List<ItemStack> drops = new ArrayList<>();
        List<Material> materials = dropsMap.getOrDefault(type, List.of());
        for (Material mat : materials) {
            int amount = (random.nextInt(3) + 1) * stackSize;
            drops.add(new ItemStack(mat, amount));
        }
        return drops;
    }

    public int calculateSellXP(EntityType type, int quantity) {
        return quantity;
    }

    public int getTotalSellXP(Block block) {
        EntityType type = getSpawnerType(block);
        if (type == null) return 0;
        List<ItemStack> drops = getVirtualDrops(type, getStackCount(block));
        int totalQuantity = drops.stream().mapToInt(ItemStack::getAmount).sum();
        return calculateSellXP(type, totalQuantity);
    }

    public SpawnerPlugin getPlugin() {
        return plugin;
    }
}
