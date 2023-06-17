package me.dave.followers.data;

import me.dave.followers.utils.ItemStackData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import me.dave.followers.Followers;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ConfigManager {
    private double speed;
    private boolean areHitboxesEnabled;
    private String nicknameFormat;
    private GuiConfig gui;
    private DatabaseConfig database;
    private final HashMap<String, String> langMessages = new HashMap<>();

    public ConfigManager() {
        Followers plugin = Followers.getInstance();
        plugin.saveDefaultConfig();

        reloadConfig(plugin);
    }

    public void reloadConfig(Followers plugin) {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        langMessages.clear();

        areHitboxesEnabled = config.getBoolean("hitboxesEnabled");
        speed = config.getDouble("speed", 0.4);
        nicknameFormat = config.getString("follower-nickname-format", "%nickname%");

        gui = new GuiConfig(config.getString("gui.title", "Followers"), config.getString("gui.follower-format", "&e%follower%"));
        database = new DatabaseConfig(config.getString("database.type"), config.getConfigurationSection("database"));

        for (String messageName : config.getConfigurationSection("messages").getKeys(false)) {
            langMessages.put(messageName, config.getString("messages." + messageName.toLowerCase()));
        }
    }

    public String getLangMessage(String messageName) {
        return langMessages.get(messageName.toLowerCase());
    }

    public ItemStack getGuiItem(String itemName) {
        return getGuiItem(itemName, Material.STONE);
    }

    public ItemStack getGuiItem(String itemName, Material def) {
        ConfigurationSection itemSection = Followers.getInstance().getConfig().getConfigurationSection("gui." + itemName);
        return ItemStackData.parse(itemSection, def);
    }

    public String getGuiTitle() {
        return gui.title;
    }

    public String getGuiFollowerFormat() {
        return gui.followerFormat;
    }

    public String getDatabaseType() {
        return database.type;
    }

    public ConfigurationSection getDatabaseSection() {
        return database.section;
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


    public record GuiConfig(String title, String followerFormat) {}
    public record DatabaseConfig(String type, ConfigurationSection section) {}
}
