package org.lushplugins.followers.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.lushlib.gui.inventory.GuiFormat;

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

        double speed = config.getDouble("speed", 0.4);
        if (speed < 0.001) {
            plugin.getLogger().warning("Found invalid speed '" + speed + "', speed must be higher than 0.001");
            speed = 0.001;
        } else if (speed > 1) {
            plugin.getLogger().warning("Found invalid speed '" + speed + "', speed must be less than or equal to 1");
            speed = 1;
        }
        this.speed = speed;

        defaultScale = config.getDouble("default-scale", 1);
        heightOffset = config.getDouble("height-offset", 0);
        nicknameFormat = config.getString("follower-nickname-format", "%nickname%");
        worldBlacklist = config.getStringList("world-blacklist");

        GuiFormat guiFormat;
        if (config.contains("menu-gui.format")) {
            guiFormat = new GuiFormat(config.getStringList("menu-gui.format"));
        } else {
             guiFormat = new GuiFormat(
                "#########",
                "FFFFFFFFF",
                "FFFFFFFFF",
                "FFFFFFFFF",
                "FFFFFFFFF",
                "NR#<T>###"
            );
        }
        // TODO: Move some static items over to DisplayItemStack and use GuiFormat#setItemReference

        gui = new GuiConfig(
            config.getString("menu-gui.title", "Followers"),
            config.getString("menu-gui.follower-format", "&e%follower%"),
            guiFormat
        );

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
        switch (guiType) {
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

    public GuiFormat getGuiFormat() {
        return gui.guiFormat;
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

    public record GuiConfig(String title, String followerFormat, GuiFormat guiFormat) {}
    public record DatabaseConfig(String type, ConfigurationSection section) {}
}