package me.dave.enchantedfollowers.datamanager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import me.dave.enchantedfollowers.Followers;


public class ConfigManager {
    private final Followers plugin = Followers.getInstance();
    private String prefix;
    private double speed;
    private boolean areHitboxesEnabled;

    public ConfigManager() {
        plugin.saveDefaultConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        prefix = config.getString("prefix", "");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        speed = config.getDouble("speed", 0.4);
        areHitboxesEnabled = config.getBoolean("hitboxesEnabled");
    }

    public ConfigurationSection getDatabaseSection() {
        return plugin.getConfig().getConfigurationSection("database");
    }

    public String getPrefix() {
        return prefix;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean areHitboxesEnabled() {
        return areHitboxesEnabled;
    }
}
