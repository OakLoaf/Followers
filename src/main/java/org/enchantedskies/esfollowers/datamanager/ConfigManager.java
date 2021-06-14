package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.enchantedskies.esfollowers.ESFollowers;


public class ConfigManager {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private FileConfiguration config;

    public ConfigManager() {
        config = plugin.getConfig();
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
    }

    public ConfigurationSection getDatabaseSection() {
        return config.getConfigurationSection("Database");
    }
}
