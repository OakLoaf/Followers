package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.enchantedskies.esfollowers.events.PetUserEvents;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public final class ESFollowers extends JavaPlugin implements Listener {
    public static NamespacedKey petKey;
    public static DataManager dataManager;
    private final HashMap<UUID, UUID> playerPetMap = new HashMap<>();
    Listener[] listeners = new Listener[] {this, new PetUserEvents(playerPetMap)};

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);
        petKey = new NamespacedKey(this, "ESFollower");

        if (!getDataFolder().exists()) getDataFolder().mkdir();
        File messagesFolder = new File(getDataFolder(), "Songs");
        if(!messagesFolder.exists()) messagesFolder.mkdirs();

        registerEvents(listeners);
        getCommand("follower").setExecutor(new Follower(this, playerPetMap));

        for (Player player : Bukkit.getOnlinePlayers()) {
            FollowerUser followerUser = dataManager.getFollowerUser(player.getUniqueId());
            String follower = followerUser.getFollower();
            if (!followerUser.isFollowerEnabled()) continue;

            if (playerPetMap.containsKey(player.getUniqueId())) continue;


        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(petKey, PersistentDataType.STRING)) entity.remove();
        }
    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
