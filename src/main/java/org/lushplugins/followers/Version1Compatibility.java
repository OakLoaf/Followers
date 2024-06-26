package org.lushplugins.followers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.lushplugins.lushlib.listener.EventListener;

public class Version1Compatibility {
    private static final NamespacedKey FOLLOWER_KEY = new NamespacedKey(Followers.getInstance(), "Follower");

    public Version1Compatibility() {
        Bukkit.getWorlds().forEach(world -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getPersistentDataContainer().has(FOLLOWER_KEY, PersistentDataType.STRING)) {
                        entity.remove();
                    }
                }
            }
        });

        new WorldListener().registerListeners();
    }

    public static class WorldListener implements EventListener {

        @EventHandler
        public void onEntitiesLoad(EntitiesLoadEvent event) {
            for (Entity entity : event.getEntities()) {
                if (entity.getPersistentDataContainer().has(FOLLOWER_KEY, PersistentDataType.STRING)) {
                    entity.remove();
                }
            }
        }
    }
}
