package org.lushplugins.followers.data;

import org.lushplugins.followers.utils.ItemStackData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.lushplugins.followers.Followers;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ConfigManager {
    private double speed;
    private boolean areHitboxesEnabled;
    private String nicknameFormat;
    private boolean forceSpawn;
    private int maxRespawnAttempts;
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
        forceSpawn = config.getBoolean("force-spawn");
        maxRespawnAttempts = config.getInt("max-respawn-attempts", 3);

        gui = new GuiConfig(config.getString("menu-gui.title", "Followers"), config.getString("menu-gui.follower-format", "&e%follower%"));
        database = new DatabaseConfig(config.getString("database.type"), config.getConfigurationSection("database"));

        for (String messageName : config.getConfigurationSection("messages").getKeys(false)) {
            langMessages.put(messageName, config.getString("messages." + messageName.toLowerCase()));
        }
    }

    public String getLangMessage(String messageName) {
        return langMessages.getOrDefault(messageName.toLowerCase(), "");
    }

    public ItemStack getGuiItem(String guiType, String itemName) {
        return getGuiItem(guiType, itemName, Material.STONE);
    }

    public ItemStack getGuiItem(String guiType, String itemName, Material def) {
        ConfigurationSection itemSection = Followers.getInstance().getConfig().getConfigurationSection(guiType + ".items." + itemName);
        return ItemStackData.parse(itemSection, def);
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

    public String getFollowerNicknameFormat() {
        return nicknameFormat;
    }

    public boolean shouldForceSpawn() {
        return forceSpawn;
    }

    public int getMaxRespawnAttempts() {
        return maxRespawnAttempts;
    }


    public record GuiConfig(String title, String followerFormat) {}
    public record DatabaseConfig(String type, ConfigurationSection section) {}
}