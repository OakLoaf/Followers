package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.enchantedskies.esfollowers.ESFollowers;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YmlStorage implements Storage {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private final File dataFile = initYML();
    private final YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

    @Override
    public FollowerUser loadFollowerUser(UUID uuid) {
        ConfigurationSection configurationSection = config.getConfigurationSection(uuid.toString());
        if (configurationSection == null) {
            configurationSection = config.createSection(uuid.toString());
            Player player = Bukkit.getPlayer(uuid);
            String playerName = player.getName();
            configurationSection.set("name", playerName);
            configurationSection.set("follower", "none");
            configurationSection.set("followerDisplayName", "Unnamed");
            configurationSection.set("followerNameEnabled", Boolean.FALSE);
            configurationSection.set("followerEnabled", Boolean.TRUE);
            plugin.saveConfig();
            return new FollowerUser(uuid, playerName, "none", "Unnamed", false, false);
        }
        String name = configurationSection.getString("name");
        String follower = configurationSection.getString("follower");
        String followerDisplayName = configurationSection.getString("followerDisplayName");
        boolean followerNameEnabled = configurationSection.getBoolean("followerNameEnabled");
        boolean followerEnabled = configurationSection.getBoolean("followerEnabled");
        return new FollowerUser(uuid, name, follower, followerDisplayName, followerNameEnabled, followerEnabled);
    }

    @Override
    public void saveFollowerUser(FollowerUser followerUser) {
        ConfigurationSection configurationSection = config.createSection(followerUser.getUUID().toString());
        configurationSection.set("name", followerUser.getUsername());
        configurationSection.set("follower", followerUser.getFollower());
        configurationSection.set("followerDisplayName", followerUser.getDisplayName());
        configurationSection.set("followerNameEnabled", followerUser.isDisplayNameEnabled());
        configurationSection.set("followerEnabled", followerUser.isFollowerEnabled());
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File initYML() {
        File dataFile = new File(plugin.getDataFolder(),"data.yml");
        try {
            if (dataFile.createNewFile()) plugin.getLogger().info("File Created: data.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataFile;
    }
}
