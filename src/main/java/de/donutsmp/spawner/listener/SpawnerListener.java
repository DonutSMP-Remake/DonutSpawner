package de.donutsmp.spawner.listener;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import de.donutsmp.spawner.SpawnerPlugin;
import de.donutsmp.spawner.ui.SpawnerUI;

public class SpawnerListener implements Listener {

    private final SpawnerPlugin plugin;

    public SpawnerListener(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (!(block.getState() instanceof CreatureSpawner)) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        SpawnerUI.openSpawnerUI(player, block);
    }
}
