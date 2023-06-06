package me.dave.followers.storage;

import me.dave.followers.data.FollowerUser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import me.dave.followers.Followers;

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
            String playerName = player.getName();
            configurationSection.set("name", playerName);
            configurationSection.set("follower", "none");
            configurationSection.set("followerDisplayName", "Unnamed");
            configurationSection.set("followerNameEnabled", Boolean.FALSE);
            configurationSection.set("followerEnabled", Boolean.TRUE);
            FollowerUser followerUser = new FollowerUser(uuid, playerName, "none", "Unnamed", false, false);
            saveFollowerUser(followerUser);
            return followerUser;
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
        fileLock.lock();
        ConfigurationSection configurationSection = config.createSection(followerUser.getUUID().toString());
        configurationSection.set("name", followerUser.getUsername());
        configurationSection.set("follower", followerUser.getFollowerType());
        configurationSection.set("followerDisplayName", followerUser.getDisplayName());
        configurationSection.set("followerNameEnabled", followerUser.isDisplayNameEnabled());
        configurationSection.set("followerEnabled", followerUser.isFollowerEnabled());
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
            if (dataFile.createNewFile()) Followers.getInstance().getLogger().info("File Created: data.yml");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        this.dataFile = dataFile;
        config = YamlConfiguration.loadConfiguration(dataFile);
        return true;
    }
}
