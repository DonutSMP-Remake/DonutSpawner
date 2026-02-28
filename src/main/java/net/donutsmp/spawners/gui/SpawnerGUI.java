
package net.donutsmp.spawners.gui;

import net.donutsmp.spawners.DonutSpawners;
import net.donutsmp.spawners.holder.SpawnerGUIHolder;
import net.donutsmp.spawners.storage.SpawnerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SpawnerGUI {
    private final DonutSpawners plugin;
    private final SpawnerData data;
    private final Inventory inventory;
    private final boolean isStorage;
    private final int page;
    private final int totalPages;

    public SpawnerGUI(DonutSpawners plugin, SpawnerData data, boolean isStorage) {
        this(plugin, data, isStorage, 1);
    }

    public SpawnerGUI(DonutSpawners plugin, SpawnerData data, boolean isStorage, int page) {
        this.plugin = plugin;
        this.data = data;
        this.isStorage = isStorage;
        this.page = page;
        int totalStacks = 0;
        if (isStorage) {
            for (Map.Entry<Material, Long> entry : data.getAccumulatedDrops().entrySet()) {
                totalStacks += (int) Math.ceil(entry.getValue() / 64.0);
            }
        }
        int calcPages = isStorage ? (int) Math.ceil(totalStacks / 45.0) : 1;
        this.totalPages = calcPages == 0 ? 1 : calcPages;

        String titleKey = isStorage ? "messages.gui_title_storage" : "messages.gui_title";
        String titleFormat = plugin.getConfig().getString(titleKey, isStorage ? "&8%stack% %type% (%page%/%pages%)" : "&8%stack% %type% SPAWNERS");

        String title = ChatColor.translateAlternateColorCodes('&', titleFormat
                .replace("%stack%", String.valueOf(data.getStackSize()))
                .replace("%type%", capitalize(data.getType().name()))
                .replace("%page%", String.valueOf(page))
                .replace("%pages%", String.valueOf(this.totalPages)));

        this.inventory = Bukkit.createInventory(new SpawnerGUIHolder(data, isStorage), isStorage ? 54 : 27, title);
        setupGUI();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void setupGUI() {
        if (isStorage) {
            setupStorage();
        } else {
            setupMain();
        }
    }
    private void setupMain() {
        ItemStack skull = new ItemStack(data.getType().getHeadMaterial());
        ItemMeta skullMeta = skull.getItemMeta();
        String spawnerNameFormat = plugin.getConfig().getString(
                "messages.gui_spawner_item_name",
                "&e%stack% %type% SPAWNERS"
        );
        skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', spawnerNameFormat
                .replace("%stack%", String.valueOf(data.getStackSize()))
                .replace("%type%", data.getType().getDisplayName().toUpperCase(Locale.ENGLISH))));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_click_to_sell", "&e&l•&r&fClick to sell items and collect xp")));

        long storedItems = data.getAccumulatedDrops().values().stream().mapToLong(Long::longValue).sum();
        long storageCapacity = Math.max(1L, plugin.getConfig().getLong("settings.storage_capacity", 1_000_000L));
        double fillPercent = Math.min(100.0D, (storedItems * 100.0D) / storageCapacity);
        String storageLine = plugin.getConfig().getString("messages.gui_storage_filled", "&estorage: &f%percent%% &eFilled")
                .replace("%percent%", String.format(Locale.ENGLISH, "%.1f", fillPercent));
        lore.add(ChatColor.translateAlternateColorCodes('&', storageLine));

        skullMeta.setLore(lore);
        skull.setItemMeta(skullMeta);
        inventory.setItem(13, skull);

        ItemStack storageItem = new ItemStack(Material.CHEST);
        ItemMeta storageMeta = storageItem.getItemMeta();
        storageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_open_storage", "&6SPAWNER STORAGE")));
        List<String> storageLore = new ArrayList<>();
        data.getAccumulatedDrops().entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .sorted(Map.Entry.<Material, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(2)
                .forEach(entry -> storageLore.add(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.gui_storage_preview_entry", "&6%amount% &f%item%")
                                .replace("%amount%", formatCompactAmount(entry.getValue()))
                                .replace("%item%", capitalizeWords(entry.getKey().name())))));

        if (storageLore.isEmpty()) {
            storageLore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_storage_preview_empty", "&7Empty")));
        }
        storageMeta.setLore(storageLore);
        storageItem.setItemMeta(storageMeta);
        inventory.setItem(11, storageItem);

        ItemStack xpBottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = xpBottle.getItemMeta();
        xpMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_collect_xp", "&aCOLLECT XP")));
        List<String> xpLore = new ArrayList<>();
        xpLore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.gui_collect_xp_points", "&a%xp% &fXP Points")
                        .replace("%xp%", formatCompactAmountWithDecimal(data.getAccumulatedXP()))));
        xpMeta.setLore(xpLore);
        xpBottle.setItemMeta(xpMeta);
        inventory.setItem(15, xpBottle);
    }


    private void setupStorage() {
        NamespacedKey pageKey = new NamespacedKey(plugin, "gui_page");
        NamespacedKey targetPageKey = new NamespacedKey(plugin, "gui_target_page");

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        List<String> backLore = new ArrayList<>();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_back", "&cBACK")));
        backLore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_back_lore", "&fClick to return to the spawner")));
        backMeta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page);
        backMeta.setLore(backLore);
        backItem.setItemMeta(backMeta);
        inventory.setItem(45, backItem);

        if (page > 1) {
            ItemStack previousArrow = new ItemStack(Material.ARROW);
            ItemMeta previousMeta = previousArrow.getItemMeta();
            previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_previous_page", "&aPREVIOUS")));
            List<String> previousLore = new ArrayList<>();
            previousLore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_previous_page_lore", "&fClick to go back a page")));
            previousMeta.setLore(previousLore);
            previousMeta.getPersistentDataContainer().set(targetPageKey, PersistentDataType.INTEGER, page - 1);
            previousArrow.setItemMeta(previousMeta);
            inventory.setItem(48, previousArrow);
        }

        ItemStack spawnerCollectItem = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta spawnerCollectMeta = spawnerCollectItem.getItemMeta();
        spawnerCollectMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_storage_collect", "&6SPAWNER")));
        List<String> spawnerCollectLore = new ArrayList<>();
        spawnerCollectLore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_storage_collect_lore", "&fCollect your loot from the storage")));
        spawnerCollectMeta.setLore(spawnerCollectLore);
        spawnerCollectItem.setItemMeta(spawnerCollectMeta);
        inventory.setItem(49, spawnerCollectItem);

        if (page < totalPages) {
            ItemStack nextArrow = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextArrow.getItemMeta();
            nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_next_page", "&eNEXT")));
            List<String> nextLore = new ArrayList<>();
            nextLore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_next_page_lore", "&fClick to go to next page")));
            nextMeta.setLore(nextLore);
            nextMeta.getPersistentDataContainer().set(targetPageKey, PersistentDataType.INTEGER, page + 1);
            nextArrow.setItemMeta(nextMeta);
            inventory.setItem(50, nextArrow);
        }

        ItemStack dropper = new ItemStack(Material.DROPPER);
        ItemMeta dropperMeta = dropper.getItemMeta();
        dropperMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_drop_all", "&aDROP LOOT")));
        List<String> dropLore = new ArrayList<>();
        dropLore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_drop_all_lore", "&fClick to drop all loot on the page")));
        dropperMeta.setLore(dropLore);
        dropper.setItemMeta(dropperMeta);
        inventory.setItem(52, dropper);

        ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT);
        ItemMeta goldMeta = goldIngot.getItemMeta();
        goldMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_sell_all", "&aSELL ALL")));
        List<String> sellAllLore = new ArrayList<>();
        sellAllLore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gui_sell_all_lore", "&7click to sell all mob drops!")));
        goldMeta.setLore(sellAllLore);
        goldIngot.setItemMeta(goldMeta);
        inventory.setItem(53, goldIngot);

        int slot = 0;
        int startIndex = (page - 1) * 45;
        int endIndex = startIndex + 45;
        int index = 0;
        for (Map.Entry<Material, Long> entry : data.getAccumulatedDrops().entrySet()) {
            Material mat = entry.getKey();
            long totalAmount = entry.getValue();
            long remaining = totalAmount;
            while (remaining > 0 && index < endIndex) {
                if (index >= startIndex) {
                    if (slot >= 45) break;
                    int stackSize = (int) Math.min(remaining, 64);
                    ItemStack item = new ItemStack(mat, stackSize);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + capitalize(mat.name()) + " x" + stackSize));
                    item.setItemMeta(itemMeta);
                    inventory.setItem(slot, item);
                    slot++;
                }
                remaining -= 64;
                index++;
            }
            if (slot >= 45) break;
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private String formatCompactAmount(long amount) {
        if (amount >= 1_000_000) {
            return String.format(Locale.ENGLISH, "%.1fM", amount / 1_000_000.0);
        }
        if (amount >= 1_000) {
            return String.format(Locale.ENGLISH, "%.0fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }

    private String formatCompactAmountWithDecimal(long amount) {
        if (amount >= 1_000_000_000) {
            return String.format(Locale.ENGLISH, "%.1fB", amount / 1_000_000_000.0);
        }
        if (amount >= 1_000_000) {
            return String.format(Locale.ENGLISH, "%.1fM", amount / 1_000_000.0);
        }
        if (amount >= 1_000) {
            return String.format(Locale.ENGLISH, "%.1fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }

    private String capitalizeWords(String input) {
        String[] parts = input.toLowerCase(Locale.ENGLISH).split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            if (i > 0) result.append(" ");
            result.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return result.toString();
    }
}
