package de.donutsmp.spawner.hologram;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {

    private final Map<Location, ArmorStand> holograms = new HashMap<>();

    public void updateHologram(Block block, int stackCount) {
        Location location = block.getLocation().add(0.5, 1.5, 0.5);

        if (holograms.containsKey(block.getLocation())) {
            ArmorStand oldStand = holograms.get(block.getLocation());
            oldStand.remove();
        }

        ArmorStand hologram = (ArmorStand) block.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        hologram.setCustomName("§6§lStack: " + stackCount + "x");
        hologram.setCustomNameVisible(true);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setInvulnerable(true);

        holograms.put(block.getLocation(), hologram);
    }

    public void removeHologram(Block block) {
        Location location = block.getLocation();
        if (holograms.containsKey(location)) {
            ArmorStand hologram = holograms.get(location);
            hologram.remove();
            holograms.remove(location);
        }
    }

    public void removeAllHolograms() {
        for (ArmorStand hologram : holograms.values()) {
            hologram.remove();
        }
        holograms.clear();
    }
}
