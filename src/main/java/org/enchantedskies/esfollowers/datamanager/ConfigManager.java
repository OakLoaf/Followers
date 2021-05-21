package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.enchantedskies.esfollowers.ESFollowers;

import java.util.HashMap;

public class ConfigManager {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private FileConfiguration config;
    private HashMap<String, FollowerHandler> followerList;

    public ConfigManager() {
        config = plugin.getConfig();
        for (String followerName : config.getKeys(false)) {
            ConfigurationSection configSection = config.getConfigurationSection(followerName);
            followerList.put(followerName, new FollowerHandler(configSection));
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        plugin.saveConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        clearFollowerCache();
        for (String followerName : config.getKeys(false)) {
            loadFollower(followerName);
        }
    }

    public void loadFollower(String followerName) {
        ConfigurationSection configurationSection = config.getConfigurationSection(followerName);
        if (followerList.containsKey(followerName)) return;
        followerList.put(followerName, new FollowerHandler(configurationSection));
    }

    public FollowerHandler getFollower(String followerName) {
        return followerList.get(followerName);
    }

    public HashMap getFollowers() {
        return followerList;
    }

    public void clearFollowerCache() {
        followerList.clear();
    }
}
