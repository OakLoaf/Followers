package me.dave.followers.data;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.utils.ItemStackData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
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

    public void createFollower(Player player, String followerName, ArmorStand armorStand) {
        ConfigurationSection configurationSection = config.getConfigurationSection(followerName);
        if (configurationSection != null) {
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-already-exists"));
            return;
        }

        configurationSection = config.createSection(followerName);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            EntityEquipment armorStandEquipment = armorStand.getEquipment();
            if (armorStandEquipment == null) return;
            String equipmentSlotName = equipmentSlot.name().toLowerCase();
            switch (equipmentSlot) {
                case HAND -> equipmentSlotName = "mainHand";
                case OFF_HAND -> equipmentSlotName = "offHand";
            }
            ItemStackData.save(armorStandEquipment.getItem(equipmentSlot), configurationSection, equipmentSlotName);
        }

        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-created").replaceAll("%follower%", followerName));
        saveFollowers();
        loadFollower(followerName);
    }

    public void createFollower(Player player, FollowerHandler followerHandler) {

    }

    public void loadFollower(String followerName) {
        ConfigurationSection configurationSection = config.getConfigurationSection(followerName);
        if (followerList.containsKey(followerName)) return;
        followerList.put(followerName, new FollowerHandler(configurationSection));
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
