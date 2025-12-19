package de.donutsmp.spawner.nms;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class DefaultNMSHandler implements NMSHandler {

    @Override
    public void setSpawnerType(CreatureSpawner spawner, EntityType type) {
        spawner.setSpawnedType(type);
    }

    @Override
    public EntityType getSpawnerType(CreatureSpawner spawner) {
        return spawner.getSpawnedType();
    }

    @Override
    public void setSpawnerDelay(CreatureSpawner spawner, int delay) {
        spawner.setDelay(delay);
    }

    @Override
    public int getSpawnerDelay(CreatureSpawner spawner) {
        return spawner.getDelay();
    }

    @Override
    public void updateSpawner(CreatureSpawner spawner) {
        spawner.update();
    }
}
