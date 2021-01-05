package org.enchantedskies.esfollowers;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.enchantedskies.esfollowers.commands.Follower;
import org.enchantedskies.esfollowers.datamanager.DataManager;
import org.enchantedskies.esfollowers.events.FollowerGUIEvents;
import org.enchantedskies.esfollowers.events.FollowerUserEvents;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class ESFollowers extends JavaPlugin implements Listener {
    public static NamespacedKey followerKey;
    public static DataManager dataManager   ;
    private final HashMap<UUID, UUID> playerFollowerMap = new HashMap<>();
    private final HashMap<String, PlayerProfile> followerSkullMap = new HashMap<>();
    private final HashSet<UUID> guiPlayerSet = new HashSet<>();
    Listener[] listeners = new Listener[] {this, new FollowerUserEvents(playerFollowerMap), new FollowerGUIEvents(guiPlayerSet)};

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);
        followerKey = new NamespacedKey(this, "ESFollower");

        writeFile();
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        registerEvents(listeners);
        getCommand("follower").setExecutor(new Follower(this, playerFollowerMap, guiPlayerSet));

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
                }
            }
        }

//        for (Player player : Bukkit.getOnlinePlayers()) {
//            FollowerUser followerUser = dataManager.getFollowerUser(player.getUniqueId());
//            String follower = followerUser.getFollower();
//            if (!followerUser.isFollowerEnabled()) continue;
//            if (playerFollowerMap.containsKey(player.getUniqueId())) continue;
//
//
//        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
        }
    }

    public void writeFile() {
        File dataFile = new File(this.getDataFolder(),"data.yml");
        try {
            if (dataFile.createNewFile()) System.out.println("File Created: data.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
