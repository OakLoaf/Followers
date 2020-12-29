package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.enchantedskies.esfollowers.commands.Follower;
import org.enchantedskies.esfollowers.datamanager.DataManager;
import org.enchantedskies.esfollowers.datamanager.FollowerUser;
import org.enchantedskies.esfollowers.events.FollowerUserEvents;

import java.util.HashMap;
import java.util.UUID;

public final class ESFollowers extends JavaPlugin implements Listener {
    public static NamespacedKey followerKey;
    public static DataManager dataManager;
    private final HashMap<UUID, UUID> playerFollowerMap = new HashMap<>();
    Listener[] listeners = new Listener[] {this, new FollowerUserEvents(playerFollowerMap)};

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);
        followerKey = new NamespacedKey(this, "ESFollower");

        if (!getDataFolder().exists()) getDataFolder().mkdir();

        registerEvents(listeners);
        getCommand("follower").setExecutor(new Follower(this, playerFollowerMap));

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
                }
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            FollowerUser followerUser = dataManager.getFollowerUser(player.getUniqueId());
            String follower = followerUser.getFollower();
            if (!followerUser.isFollowerEnabled()) continue;

            if (playerFollowerMap.containsKey(player.getUniqueId())) continue;


        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
        }
    }

//    @EventHandler
//    public void onEntityLoad(EntityAddToWorldEvent event) {
//        Entity entity = event.getEntity();
//        if (entity.getType() != EntityType.ARMOR_STAND) return;
//        if (!entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) return;
//        new BukkitRunnable() {
//            public void run() {
//                entity.remove();
//            }
//        }.runTaskLater(this, 1L);
//    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
