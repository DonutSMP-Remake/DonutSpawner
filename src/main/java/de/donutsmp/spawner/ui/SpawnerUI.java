package de.donutsmp.spawner.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import de.donutsmp.spawner.SpawnerPlugin;
import de.donutsmp.spawner.manager.SpawnerManager;
import de.donutsmp.spawner.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class SpawnerUI {

    private static final SpawnerManager manager = SpawnerPlugin.getInstance().getSpawnerManager();

    public static void openSpawnerUI(Player player, Block block) {
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        int stackCount = manager.getStackCount(block);
        EntityType spawnerTypeEnum = manager.getSpawnerType(block);
        String spawnerType = spawnerTypeEnum != null ? spawnerTypeEnum.name() : "UNKNOWN";

        Inventory inventory = Bukkit.createInventory(null, 27, "§5§l" + stackCount + " Spawner");

        ItemStack mobHead = createMobHead(spawnerTypeEnum);
        inventory.setItem(13, mobHead);

        ItemStack xpBottle = createButton(Material.EXPERIENCE_BOTTLE, "§b§lXP Collection", "§7Click to collect XP only");
        inventory.setItem(15, xpBottle);

        ItemStack chestItem = createButton(Material.CHEST, "§e§lSell Items", "§7Open selling UI");
        inventory.setItem(11, chestItem);

        player.setMetadata("spawner_block_x", new org.bukkit.metadata.FixedMetadataValue(SpawnerPlugin.getInstance(), block.getX()));
        player.setMetadata("spawner_block_y", new org.bukkit.metadata.FixedMetadataValue(SpawnerPlugin.getInstance(), block.getY()));
        player.setMetadata("spawner_block_z", new org.bukkit.metadata.FixedMetadataValue(SpawnerPlugin.getInstance(), block.getZ()));
        player.setMetadata("spawner_block_world", new org.bukkit.metadata.FixedMetadataValue(SpawnerPlugin.getInstance(), block.getWorld().getName()));

        player.openInventory(inventory);
    }

    public static void openSellingUI(Player player, Block block, int page) {
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        EntityType spawnerTypeEnum = manager.getSpawnerType(block);
        String spawnerType = spawnerTypeEnum != null ? spawnerTypeEnum.name() : "UNKNOWN";
        int stackCount = manager.getStackCount(block);

        Inventory inventory = Bukkit.createInventory(null, 54, "§6§lSell Items from " + spawnerType);

        List<ItemStack> drops = manager.getVirtualDrops(spawnerTypeEnum, stackCount);
        for (int i = 0; i < Math.min(drops.size(), 45); i++) {
            inventory.setItem(i, drops.get(i));
        }

        ItemStack close = createButton(Material.BARRIER, "§c§lClose", "§7Close selling UI");
        inventory.setItem(45, close);

        ItemStack prev = createButton(Material.ARROW, "§7<< Previous", "§7No previous page");
        inventory.setItem(48, prev);

        ItemStack back = createButton(Material.PAPER, "§e§lBack to Spawner", "§7Return to main menu");
        inventory.setItem(49, back);

        ItemStack next = createButton(Material.ARROW, "§7Next >>", "§7No next page");
        inventory.setItem(50, next);

        int totalXP = manager.getTotalSellXP(block);
        ItemStack instantSell = createButton(Material.GOLD_INGOT, "§6§lInstant Sell All", "§7Get " + totalXP + " XP instantly");
        inventory.setItem(53, instantSell);

        player.setMetadata("selling_page", new org.bukkit.metadata.FixedMetadataValue(SpawnerPlugin.getInstance(), page));

        player.openInventory(inventory);
    }

    private static ItemStack createMobHead(EntityType type) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§f§l" + (type != null ? type.name() : "Unknown") + " Head");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to open selling UI");
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private static ItemStack createInfoItem(String spawnerType, int stackCount, int delay) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§lSpawner Information");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Type: §e" + spawnerType);
            lore.add("§7Stack Count: §e" + stackCount + "x");
            lore.add("§7Delay: §e" + delay + "ms");
            lore.add("");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);

            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }

            meta.setLore(loreList);
            item.setItemMeta(meta);
        }

        return item;
    }
}
