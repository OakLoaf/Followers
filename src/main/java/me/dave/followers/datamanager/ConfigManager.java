package me.dave.followers.datamanager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import me.dave.followers.Followers;
import org.bukkit.inventory.ItemStack;

public class ConfigManager {
    private final Followers plugin = Followers.getInstance();
    private FileConfiguration config;
    private String prefix;
    private double speed;
    private boolean areHitboxesEnabled;
    private String nicknameFormat;

    public ConfigManager() {
        plugin.saveDefaultConfig();

        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        prefix = config.getString("prefix", "");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        areHitboxesEnabled = config.getBoolean("hitboxesEnabled");
        speed = config.getDouble("speed", 0.4);
        nicknameFormat = config.getString("follower-nickname-format", "%nickname%");
    }

    public String getLangMessage(String messageName) {
        return prefix + config.getString("messages." + messageName.toLowerCase());
    }

    public ItemStack getGuiItem(String itemName) {
        ConfigurationSection itemSection = config.getConfigurationSection("gui." + itemName);
        if (itemSection == null) return new ItemStack(Material.STONE);

        return new ItemStackData(itemSection, "STONE").getItem();
    }

    public String getGuiTitle() {
        return config.getString("gui.title", "Followers");
    }

    public String getGuiFollowerFormat() {
        return config.getString("gui.follower-format", "&e%follower%");
    }

    public ConfigurationSection getDatabaseSection() {
        return config.getConfigurationSection("database");
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean areHitboxesEnabled() {
        return areHitboxesEnabled;
    }

    public double getSpeed() {
        return speed;
    }

    public String getFollowerNicknameFormat() {
        return nicknameFormat;
    }
}
