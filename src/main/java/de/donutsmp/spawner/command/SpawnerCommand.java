package de.donutsmp.spawner.command;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import de.donutsmp.spawner.SpawnerPlugin;
import de.donutsmp.spawner.ui.SpawnerUI;
import de.donutsmp.spawner.manager.SpawnerManager;

public class SpawnerCommand implements CommandExecutor {

    private final SpawnerPlugin plugin;
    private final NamespacedKey stackKey;

    public SpawnerCommand(SpawnerPlugin plugin) {
        this.plugin = plugin;
        this.stackKey = new NamespacedKey(plugin, "spawner_stack");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("spawner")) {
            if (!player.hasPermission("donutsmp.spawner.use")) {
                player.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            Block target = player.getTargetBlock(null, 5);
            if (target != null && target.getType() == Material.SPAWNER) {
                SpawnerUI.openSpawnerUI(player, target);
            } else {
                player.sendMessage("§cLook at a spawner to open its menu!");
            }
            return true;
        } else if (label.equalsIgnoreCase("spawnergive")) {
            if (!player.hasPermission("donutsmp.spawner.give")) {
                player.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("§cUsage: /spawnergive <type> <amount>");
                player.sendMessage("§cTypes: SKELETON, ZOMBIE, SPIDER, etc.");
                return true;
            }

            EntityType type;
            try {
                type = EntityType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid entity type: " + args[0]);
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0 || amount > 64) {
                    player.sendMessage("§cAmount must be between 1 and 64!");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount: " + args[1]);
                return true;
            }

            ItemStack spawnerItem = new ItemStack(Material.SPAWNER, amount);
            BlockStateMeta meta = (BlockStateMeta) spawnerItem.getItemMeta();
            if (meta != null) {
                CreatureSpawner spawnerMeta = (CreatureSpawner) meta.getBlockState();
                spawnerMeta.setSpawnedType(type);
                meta.setBlockState(spawnerMeta);
                if (amount > 1) {
                    meta.getPersistentDataContainer().set(stackKey, PersistentDataType.INTEGER, amount);
                }
                spawnerItem.setItemMeta(meta);
            }

            player.getInventory().addItem(spawnerItem);
            player.sendMessage("§aGave " + amount + " " + type.name() + " spawners!");
            return true;
        }

        return false;
    }
}
