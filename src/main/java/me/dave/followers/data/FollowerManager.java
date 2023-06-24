package me.dave.followers.data;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.utils.ItemStackData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import me.dave.followers.Followers;

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
        Bukkit.getOnlinePlayers().forEach(player -> {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            if (followerUser == null) return;
            followerUser.refreshFollowerEntity();
        });
    }

    public void createFollower(Player player, FollowerHandler followerHandler) {
        createFollower(player, followerHandler, false);
    }

    public void createFollower(Player player, FollowerHandler followerHandler, boolean replace) {
        String followerName = followerHandler.getName();
        ConfigurationSection configurationSection = config.getConfigurationSection(followerName);
        if (!replace && configurationSection != null) {
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-already-exists"));
            return;
        }

        configurationSection = config.createSection(followerName);
        ItemStackData.save(followerHandler.getHead(), configurationSection, "head");
        ItemStackData.save(followerHandler.getChest(), configurationSection, "chest");
        ItemStackData.save(followerHandler.getLegs(), configurationSection, "legs");
        ItemStackData.save(followerHandler.getFeet(), configurationSection, "feet");
        ItemStackData.save(followerHandler.getMainHand(), configurationSection, "mainHand");
        ItemStackData.save(followerHandler.getOffHand(), configurationSection, "offHand");

        configurationSection.set("visible", followerHandler.isVisible());

        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-created").replaceAll("%follower%", followerName));
        saveFollowers();
        loadFollower(followerName, followerHandler);
        Followers.followerManager.refreshAllFollowers();
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

    public List<String> getFollowerNames(Player player) {
        List<String> followers = new ArrayList<>();
        for (String followerName : followerList.keySet()) {
            if (!player.hasPermission("followers." + followerName.toLowerCase().replaceAll(" ", "_"))) continue;
            followers.add(followerName);
        }
        return followers;
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
