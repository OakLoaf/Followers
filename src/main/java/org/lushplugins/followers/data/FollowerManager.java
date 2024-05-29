package org.lushplugins.followers.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FollowerManager {
    private final File followerConfigFile = initYML();
    private YamlConfiguration config = YamlConfiguration.loadConfiguration(followerConfigFile);
    private final Map<String, FollowerHandler> followerList = new TreeMap<>();

    public FollowerManager() {
        for (String followerName : config.getKeys(false)) {
            loadFollower(followerName);
        }
    }

    public void saveFollowers() {
        try {
            config.save(followerConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadFollowers() {
        clearFollowerCache();
        config = YamlConfiguration.loadConfiguration(followerConfigFile);
        for (String followerName : config.getKeys(false)) {
            loadFollower(followerName);
        }
    }

    public void refreshAllFollowers() {
        Followers.getInstance().getDataManager().getOnlineFollowerUsers().forEach(FollowerUser::refreshFollower);
    }

    public void createFollower(Player player, FollowerHandler followerHandler) {
        createFollower(player, followerHandler, false);
    }

    public void createFollower(Player player, FollowerHandler followerHandler, boolean replace) {
        String followerName = followerHandler.getName();
        ConfigurationSection configurationSection = config.getConfigurationSection(followerName);
        if (!replace && configurationSection != null) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-already-exists"));
            return;
        }

        configurationSection = config.createSection(followerName);
        configurationSection.set("entityType", followerHandler.getEntityType().getName());
        followerHandler.getHead().save(configurationSection, "head");
        followerHandler.getChest().save(configurationSection, "chest");
        followerHandler.getLegs().save(configurationSection, "legs");
        followerHandler.getFeet().save(configurationSection, "feet");
        followerHandler.getMainHand().save(configurationSection, "mainHand");
        followerHandler.getOffHand().save(configurationSection, "offHand");
        configurationSection.set("visible", followerHandler.isVisible());
        configurationSection.set("scale", followerHandler.getScale());

        if (!replace) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-created")
                .replaceAll("%follower%", followerName));
        } else {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-edited")
                .replaceAll("%follower%", followerName));
        }

        saveFollowers();
        loadFollower(followerName, followerHandler);
        Followers.getInstance().getFollowerManager().refreshAllFollowers();
    }

    public void editFollower(Player player, FollowerHandler followerHandler) {
        createFollower(player, followerHandler, true);
    }

    public void loadFollower(String followerName) {
        ConfigurationSection configurationSection = config.getConfigurationSection(followerName);
        if (configurationSection == null) {
            Followers.getInstance().getLogger().severe("Tried to load follower \"" + followerName + "\" but data for this follower could not be found");
            return;
        }
        followerList.put(followerName, new FollowerHandler(configurationSection));
    }

    public void loadFollower(String followerName, FollowerHandler followerHandler) {
        followerList.put(followerName, followerHandler);
    }

    public void removeFollower(String followerName) {
        config.set(followerName, null);
        followerList.remove(followerName);
        saveFollowers();
    }

    public FollowerHandler getFollower(String followerName) {
        return followerList.get(followerName);
    }

    public Map<String, FollowerHandler> getFollowers() {
        return followerList;
    }

    public Set<String> getFollowerNames() {
        return followerList.keySet();
    }

    public void clearFollowerCache() {
        followerList.clear();
    }

    private File initYML() {
        Followers plugin = Followers.getInstance();
        File followerConfigFile = new File(plugin.getDataFolder(),"followers.yml");
        if (!followerConfigFile.exists()) {
            plugin.saveResource("followers.yml", false);
            plugin.getLogger().info("File Created: followers.yml");
        }
        return followerConfigFile;
    }
}
