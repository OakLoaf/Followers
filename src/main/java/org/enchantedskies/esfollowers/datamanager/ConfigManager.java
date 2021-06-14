package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.enchantedskies.esfollowers.ESFollowers;


public class ConfigManager {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private FileConfiguration config;

    public ConfigManager() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
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
        return config.getConfigurationSection("database");
    }

    public String getPrefix() {
        String prefix = config.getString("prefix");
        if (prefix == null) prefix = "";
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        return prefix;
    }
}
