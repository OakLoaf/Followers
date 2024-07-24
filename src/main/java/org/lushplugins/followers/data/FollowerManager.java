package org.lushplugins.followers.data;

import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.EyeHeightRegistry;
import org.lushplugins.followers.entity.poses.FollowerPoseRegistry;
import org.lushplugins.followers.entity.tasks.FollowerTaskRegistry;
import org.lushplugins.followers.utils.Converter;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.SkinData;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FollowerManager {
    private static final FollowerTaskRegistry TASK_REGISTRY = new FollowerTaskRegistry();
    private static final FollowerPoseRegistry POSE_REGISTRY = new FollowerPoseRegistry();

    private final File followerConfigFile = initYML();
    private YamlConfiguration config;
    private final Map<String, FollowerHandler> followerList = new TreeMap<>();

    public FollowerManager() {
        reloadFollowers();
    }

    public FollowerTaskRegistry getTaskRegistry() {
        return TASK_REGISTRY;
    }

    public FollowerPoseRegistry getPoseRegistry() {
        return POSE_REGISTRY;
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
        EyeHeightRegistry eyeHeightRegistry = Followers.getInstance().getEyeHeightRegistry();
        eyeHeightRegistry.clear();

        config = YamlConfiguration.loadConfiguration(followerConfigFile);
        for (String followerName : config.getKeys(false)) {
            ConfigurationSection followerSection = config.getConfigurationSection(followerName);
            if (followerSection == null) {
                Followers.getInstance().getLogger().severe("Tried to load follower \"" + followerName + "\" but data for this follower could not be found");
                return;
            }

            loadFollower(followerSection);
        }

        eyeHeightRegistry.loadEyeHeights(
            followerList.values().stream().map(FollowerHandler::getEntityType).distinct().toList()
        );
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
        configurationSection.set("entityType", followerHandler.getEntityType().getName().toString());

        SkinData skin = followerHandler.getSkin();
        if (skin != null) {
            configurationSection.set("skin", skin.getValue());
            configurationSection.set("skin-signature", skin.getSignature());
        }

        for (Map.Entry<EquipmentSlot, ExtendedSimpleItemStack> entry : followerHandler.getEquipment().entrySet()) {
            EquipmentSlot slot = entry.getKey();
            ExtendedSimpleItemStack item = entry.getValue();
            if (item == null) {
                continue;
            }

            item.save(configurationSection, Converter.getEquipmentSlotName(slot));
        }

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

    public void loadFollower(@NotNull ConfigurationSection configurationSection) {
        followerList.put(configurationSection.getName(), new FollowerHandler(configurationSection));
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
