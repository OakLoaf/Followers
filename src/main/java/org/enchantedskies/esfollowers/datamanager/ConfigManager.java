package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.configuration.file.FileConfiguration;
import org.enchantedskies.esfollowers.ESFollowers;

public class ConfigManager {
    private final ESFollowers plugin;
    private FileConfiguration config;

    public ConfigManager(ESFollowers instance) {
        plugin = instance;
        config = instance.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
}
