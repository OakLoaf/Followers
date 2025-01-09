package org.lushplugins.followers.storage;

import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.Followers;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class YmlStorage implements Storage {
    private File dataFile;
    private YamlConfiguration config;
    private final ReentrantLock fileLock = new ReentrantLock();

    @Override
    public FollowerUser loadFollowerUser(UUID uuid) {
        ConfigurationSection configurationSection = config.getConfigurationSection(uuid.toString());
        if (configurationSection == null) {
            configurationSection = config.createSection(uuid.toString());
            Player player = Bukkit.getPlayer(uuid);
            configurationSection.set("name", player.getName());
            configurationSection.set("follower", "none");
            configurationSection.set("followerDisplayName", Followers.getInstance().getConfigManager().getDefaultNickname());
            configurationSection.set("followerNameEnabled", Boolean.FALSE);
            configurationSection.set("followerEnabled", Boolean.TRUE);
            configurationSection.set("randomFollower", Boolean.FALSE);
            FollowerUser followerUser = new FollowerUser(uuid, player.getName(), "none", Followers.getInstance().getConfigManager().getDefaultNickname(), false, false, false);
            saveFollowerUser(followerUser);
            return followerUser;
        }
        String name = configurationSection.getString("name");
        String follower = configurationSection.getString("follower");
        String followerDisplayName = configurationSection.getString("followerDisplayName");
        boolean followerNameEnabled = configurationSection.getBoolean("followerNameEnabled");
        boolean followerEnabled = configurationSection.getBoolean("followerEnabled");
        boolean randomType = configurationSection.getBoolean("randomFollower");
        return new FollowerUser(uuid, name, follower, followerDisplayName, followerNameEnabled, followerEnabled, randomType);
    }

    @Override
    public void saveFollowerUser(FollowerUser followerUser) {
        fileLock.lock();
        ConfigurationSection configurationSection = config.createSection(followerUser.getUniqueId().toString());
        configurationSection.set("name", followerUser.getUsername());
        configurationSection.set("follower", followerUser.getFollowerTypeName());
        configurationSection.set("followerDisplayName", followerUser.getDisplayName());
        configurationSection.set("followerNameEnabled", followerUser.isDisplayNameEnabled());
        configurationSection.set("followerEnabled", followerUser.isFollowerEnabled());
        configurationSection.set("randomFollower", followerUser.isRandomType());
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileLock.unlock();
        }
    }

    @Override
    public boolean init() {
        File dataFile = new File(Followers.getInstance().getDataFolder(),"data.yml");

        try {
            if (dataFile.createNewFile()) {
                Followers.getInstance().getLogger().info("File Created: data.yml");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        this.dataFile = dataFile;
        config = YamlConfiguration.loadConfiguration(dataFile);
        return true;
    }
}
