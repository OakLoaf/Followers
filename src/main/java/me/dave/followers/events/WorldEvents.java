package me.dave.followers.events;

import me.dave.followers.Followers;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldEvents implements Listener {
    private static final NamespacedKey followerKey = new NamespacedKey(Followers.getInstance(), "Follower");

    @EventHandler
    public void onEntityLoad(CreatureSpawnEvent event) {
        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> {
            Entity entity = event.getEntity();
            if (entity.getType() != EntityType.ARMOR_STAND) {
                return;
            }

            if (Followers.dataManager.getActiveArmorStandsSet().contains(entity.getUniqueId())) {
                return;
            }

            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
                entity.remove();
            }
        }, 1);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        int[] entityLoadAttempt = new int[1];
        new BukkitRunnable() {
            @Override
            public void run() {
                entityLoadAttempt[0] += 1;
                if (entityLoadAttempt[0] >= 24) {
                    cancel();
                    return;
                }
                if (event.getChunk().isEntitiesLoaded()) {
                    Entity[] entities = event.getChunk().getEntities();
                    for (Entity entity : entities) {
                        if (entity.getType() != EntityType.ARMOR_STAND) {
                            continue;
                        }

                        if (Followers.dataManager.getActiveArmorStandsSet().contains(entity.getUniqueId())) {
                            continue;
                        }

                        if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
                            entity.remove();
                        }
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Followers.getInstance(), 50, 100);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.getChunk().isEntitiesLoaded()) {
            Entity[] entities = event.getChunk().getEntities();
            for (Entity entity : entities) {
                if (entity.getType() != EntityType.ARMOR_STAND) {
                    continue;
                }

                Followers.dataManager.getActiveArmorStandsSet().remove(entity.getUniqueId());
                if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
                    entity.remove();
                }
            }
        }
    }
}
