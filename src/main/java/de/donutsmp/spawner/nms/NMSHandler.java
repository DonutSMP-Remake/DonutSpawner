package de.donutsmp.spawner.nms;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public interface NMSHandler {

    void setSpawnerType(CreatureSpawner spawner, EntityType type);

    EntityType getSpawnerType(CreatureSpawner spawner);

    void setSpawnerDelay(CreatureSpawner spawner, int delay);

    int getSpawnerDelay(CreatureSpawner spawner);

    void updateSpawner(CreatureSpawner spawner);
}
