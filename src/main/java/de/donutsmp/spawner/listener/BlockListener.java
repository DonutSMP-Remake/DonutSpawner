package de.donutsmp.spawner.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.Location;
import de.donutsmp.spawner.SpawnerPlugin;
import de.donutsmp.spawner.config.ConfigManager;
import de.donutsmp.spawner.manager.SpawnerManager;

public class BlockListener implements Listener {

    private final SpawnerManager manager;

    public BlockListener(SpawnerManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        SpawnerPlugin plugin = SpawnerPlugin.getInstance();
        ConfigManager config = plugin.getConfigManager();

        if (!config.allowBreakExistingSpawners() && manager.getStackCount(block) == 1) {
            event.setCancelled(true);
            player.sendMessage("§cBreaking existing spawners is disabled!");
            return;
        }

        if (config.requireSilkTouch() && !player.getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            event.setCancelled(true);
            player.sendMessage("§cSilk Touch is required to break spawners!");
            return;
        }

        event.setDropItems(false);

        if (player.isSneaking()) {
            if (!player.hasPermission("donutsmp.spawner.stack")) {
                event.setCancelled(true);
                return;
            }
            int stack = manager.getStackCount(block);
            EntityType type = manager.getSpawnerType(block);
            int fullStacks = stack / 64;
            int remainder = stack % 64;
            Location loc = block.getLocation().add(0.5, 0.5, 0.5);
            ItemStack spawnerItem = new ItemStack(Material.SPAWNER, 64);
            if (type != null) {
                BlockStateMeta meta = (BlockStateMeta) spawnerItem.getItemMeta();
                if (meta != null) {
                    CreatureSpawner spawnerMeta = (CreatureSpawner) meta.getBlockState();
                    spawnerMeta.setSpawnedType(type);
                    meta.setBlockState(spawnerMeta);
                    spawnerItem.setItemMeta(meta);
                }
            }
            for (int i = 0; i < fullStacks; i++) {
                block.getWorld().dropItemNaturally(loc, spawnerItem.clone());
            }
            if (remainder > 0) {
                ItemStack remItem = spawnerItem.clone();
                remItem.setAmount(remainder);
                block.getWorld().dropItemNaturally(loc, remItem);
            }
            block.setType(Material.AIR);
            manager.updateHologram(block);
            player.sendMessage("§aSpawner broken! Dropped " + stack + " spawners.");
        } else {
            if (!player.hasPermission("donutsmp.spawner.stack")) {
                event.setCancelled(true);
                return;
            }
            int stack = manager.getStackCount(block);
            EntityType type = manager.getSpawnerType(block);
            ItemStack drop = new ItemStack(Material.SPAWNER, 1);
            if (type != null) {
                BlockStateMeta meta = (BlockStateMeta) drop.getItemMeta();
                if (meta != null) {
                    CreatureSpawner spawnerMeta = (CreatureSpawner) meta.getBlockState();
                    spawnerMeta.setSpawnedType(type);
                    meta.setBlockState(spawnerMeta);
                    drop.setItemMeta(meta);
                }
            }
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), drop);
            manager.setStackCount(block, stack - 1);
            if (manager.getStackCount(block) <= 0) {
                block.setType(Material.AIR);
            }
            manager.updateHologram(block);
            player.sendMessage("§aRemoved 1 from stack. Remaining: " + manager.getStackCount(block));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("donutsmp.spawner.stack")) {
            return;
        }

        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.SPAWNER || item.getAmount() < 1) {
            return;
        }

        boolean stacked = false;
        BlockState state = block.getState();
        if (state instanceof CreatureSpawner) {
            for (BlockFace face : BlockFace.values()) {
                if (face == BlockFace.SELF) continue;
                Block adjacent = block.getRelative(face);
                if (adjacent.getType() == Material.SPAWNER && manager.getStackCount(adjacent) < manager.getPlugin().getConfigManager().getMaxStackSize()) {
                    if (manager.addToStack(adjacent)) {
                        stacked = true;
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(null);
                        }
                        event.setCancelled(true);
                        manager.updateHologram(adjacent);
                        player.sendMessage("§aSpawner stacked into adjacent!");
                        break;
                    }
                }
            }
        }

        if (!stacked) {
            manager.setStackCount(block, 1);
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            if (spawner.getSpawnedType() == null) {
                spawner.setSpawnedType(EntityType.ZOMBIE);
                spawner.update();
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("donutsmp.spawner.stack") || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.SPAWNER) {
            return;
        }

        int addAmount = item.getAmount();
        int currentStack = manager.getStackCount(block);
        int maxStack = SpawnerPlugin.getInstance().getConfigManager().getMaxStackSize();
        if (currentStack + addAmount > maxStack) {
            addAmount = maxStack - currentStack;
            if (addAmount <= 0) {
                player.sendMessage("§cStack is already at maximum!");
                return;
            }
        }

        manager.setStackCount(block, currentStack + addAmount);
        item.setAmount(item.getAmount() - addAmount);
        if (item.getAmount() <= 0) {
            player.getInventory().setItemInMainHand(null);
        }

        event.setCancelled(true);
        manager.updateHologram(block);
        player.sendMessage("§aAdded " + addAmount + " to stack. Total: " + manager.getStackCount(block));
    }
}
