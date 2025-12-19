package de.donutsmp.spawner.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import de.donutsmp.spawner.SpawnerPlugin;
import de.donutsmp.spawner.manager.SpawnerManager;
import de.donutsmp.spawner.ui.SpawnerUI;


public class InventoryListener implements Listener {

    private final SpawnerManager manager;

    public InventoryListener(SpawnerManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.contains("Spawner") && !title.contains("Sell Items")) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        Block block = getBlockFromMetadata(player);
        if (block == null) {
            player.closeInventory();
            return;
        }

        EntityType spawnerType = manager.getSpawnerType(block);

        if (title.contains("Spawner")) {
            switch (slot) {
                case 11:
                    SpawnerUI.openSellingUI(player, block, 0);
                    break;

                case 13:
                    int totalXP = manager.getTotalSellXP(block);
                    player.giveExp(totalXP);
                    player.sendMessage("§a✓ Sold all for " + totalXP + " XP!");
                    manager.setStackCount(block, 0);
                    manager.updateHologram(block);
                    SpawnerUI.openSpawnerUI(player, block);
                    break;

                case 15:
                    int totalXP2 = manager.getTotalSellXP(block);
                    player.giveExp(totalXP2);
                    player.sendMessage("§a✓ Collected " + totalXP2 + " XP!");
                    manager.setStackCount(block, 0);
                    manager.updateHologram(block);
                    SpawnerUI.openSpawnerUI(player, block);
                    break;
            }
        } else {
            switch (slot) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8:
                case 9: case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17:
                case 18: case 19: case 20: case 21: case 22: case 23: case 24: case 25: case 26:
                case 27: case 28: case 29: case 30: case 31: case 32: case 33: case 34: case 35:
                case 36: case 37: case 38: case 39: case 40: case 41: case 42: case 43: case 44:
                    player.sendMessage("§cIndividual selling not implemented for virtual spawners!");
                    break;

                case 45:
                    player.closeInventory();
                    break;

                case 48:
                    player.sendMessage("§7No previous page available.");
                    break;

                case 49:
                    SpawnerUI.openSpawnerUI(player, block);
                    break;

                case 50:
                    player.sendMessage("§7No next page available.");
                    break;

                case 53:
                    int totalSellXP = manager.getTotalSellXP(block);
                    player.giveExp(totalSellXP);
                    player.sendMessage("§a✓ Instant sold all for " + totalSellXP + " XP!");
                    manager.setStackCount(block, 0);
                    manager.updateHologram(block);
                    SpawnerUI.openSellingUI(player, block, 0);
                    break;
            }
        }
    }

    private Block getBlockFromMetadata(Player player) {
        try {
            if (player.getMetadata("spawner_block_x").isEmpty()) {
                return null;
            }

            int x = player.getMetadata("spawner_block_x").get(0).asInt();
            int y = player.getMetadata("spawner_block_y").get(0).asInt();
            int z = player.getMetadata("spawner_block_z").get(0).asInt();
            String worldName = player.getMetadata("spawner_block_world").get(0).asString();

            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }

            return world.getBlockAt(x, y, z);
        } catch (Exception e) {
            return null;
        }
    }
}
