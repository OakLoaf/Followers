package org.lushplugins.followers.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;

import java.util.HashMap;
import java.util.List;

public class ConfigManager {
    private double speed;
    private double defaultScale;
    private double heightOffset;
    private boolean areHitboxesEnabled;
    private String nicknameFormat;
    private List<String> worldBlacklist;
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
        defaultScale = config.getDouble("default-scale", 1);
        heightOffset = config.getDouble("height-offset", 0);
        nicknameFormat = config.getString("follower-nickname-format", "%nickname%");
        worldBlacklist = config.getStringList("world-blacklist");

        gui = new GuiConfig(config.getString("menu-gui.title", "Followers"), config.getString("menu-gui.follower-format", "&e%follower%"));
        database = new DatabaseConfig(config.getString("database.type"), config.getConfigurationSection("database"));

        for (String messageName : config.getConfigurationSection("messages").getKeys(false)) {
            langMessages.put(messageName, config.getString("messages." + messageName.toLowerCase()));
        }
    }

    public HashMap<String, String> getLangMessages() {
        return langMessages;
    }

    public String getLangMessage(String messageName) {
        return langMessages.getOrDefault(messageName.toLowerCase(), "");
    }

    public ExtendedSimpleItemStack getGuiItem(String guiType, String itemName) {
        return getGuiItem(guiType, itemName, Material.STONE);
    }

    public ExtendedSimpleItemStack getGuiItem(String guiType, String itemName, Material def) {
        ConfigurationSection itemSection = Followers.getInstance().getConfig().getConfigurationSection(guiType + ".items." + itemName);
        ExtendedSimpleItemStack simpleItemStack = new ExtendedSimpleItemStack(itemSection);
        return simpleItemStack.hasType() ? simpleItemStack : new ExtendedSimpleItemStack(def);
    }

    public String getGuiTitle(String guiType) {
        switch(guiType) {
            case "menu-gui" -> {
                return gui.title;
            }
            case "builder-gui" -> {
                return Followers.getInstance().getConfig().getString("builder-gui.title");
            }
            case "moderation-gui" -> {
                return Followers.getInstance().getConfig().getString("moderation-gui.title");
            }
        }
        return null;
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

    public double getDefaultScale() {
        return defaultScale;
    }

    public double getHeightOffset() {
        return heightOffset;
    }

    public String getFollowerNicknameFormat() {
        return nicknameFormat;
    }

    public List<String> getWorldBlacklist() {
        return worldBlacklist;
    }

    public record GuiConfig(String title, String followerFormat) {}
    public record DatabaseConfig(String type, ConfigurationSection section) {}
}
