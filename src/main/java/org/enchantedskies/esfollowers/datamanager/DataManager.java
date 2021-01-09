package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.enchantedskies.esfollowers.ESFollowers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class DataManager {
    ESFollowers plugin;
    YamlConfiguration config;
    File dataFile;
    HashMap<UUID, FollowerUser> uuidToFollowerUser = new HashMap<>();

    public DataManager(ESFollowers instance) {
        plugin = instance;
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadFollowerUser(UUID uuid) {
        ConfigurationSection configurationSection = config.getConfigurationSection(uuid.toString());
        if (configurationSection == null) {
            configurationSection = config.createSection(uuid.toString());
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            String playerName = player.getName();
            configurationSection.set("name", playerName);
            configurationSection.set("follower", "none");
            configurationSection.set("followerEnabled", Boolean.TRUE);
            plugin.saveConfig();
            uuidToFollowerUser.put(uuid, new FollowerUser(uuid, playerName, "none", true));
            return;
        }
        String name = configurationSection.getString("name");
        String follower = configurationSection.getString("follower");
        boolean followerEnabled = configurationSection.getBoolean("followerEnabled");
        FollowerUser followerUser = new FollowerUser(uuid, name, follower, followerEnabled);
        uuidToFollowerUser.put(uuid, followerUser);
    }

    public FollowerUser getFollowerUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return uuidToFollowerUser.getOrDefault(uuid, new FollowerUser(uuid, player.getName(), "none", false));
    }

    public void saveFollowerUser(FollowerUser followerUser) {
        ConfigurationSection configurationSection = config.createSection(followerUser.getUUID().toString());
        configurationSection.set("name", followerUser.getUsername());
        configurationSection.set("follower", followerUser.getFollower());
        configurationSection.set("followerEnabled", followerUser.isFollowerEnabled());
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
