package me.dave.followers.datamanager;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import me.dave.followers.Followers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class FollowerManager {
    private final Followers plugin = Followers.getInstance();
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

    public void createFollower(Player owner, String followerName, ArmorStand armorStand) {
        ConfigurationSection configurationSection = config.getConfigurationSection(followerName);
        String prefix = Followers.configManager.getPrefix();
        if (configurationSection != null) {
            ChatColorHandler.sendMessage(owner, Followers.configManager.getLangMessage("follower-already-exists"));
            return;
        }
        configurationSection = config.createSection(followerName);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            EntityEquipment armorStandEquipment = armorStand.getEquipment();
            if (armorStandEquipment == null) return;
            ItemStack currItem = armorStandEquipment.getItem(equipmentSlot);
            Material material = currItem.getType();
            if (material == Material.AIR) continue;
            String equipmentSlotName = makeFriendly(equipmentSlot.name());
            switch (equipmentSlotName) {
                case "Hand" -> equipmentSlotName = "MainHand";
                case "Off_hand" -> equipmentSlotName = "OffHand";
            }
            configurationSection.set(equipmentSlotName + ".Material", material.toString().toLowerCase());
            if (currItem.getType() == Material.PLAYER_HEAD) {
                SkullMeta skullMeta = (SkullMeta) currItem.getItemMeta();
                OfflinePlayer skullOwner = skullMeta.getOwningPlayer();
                if (skullOwner == null) {
                    configurationSection.set(makeFriendly(equipmentSlotName) + ".SkullType", "Custom");
                    String textureStr = Followers.skullCreator.getB64(currItem);
                    configurationSection.set(makeFriendly(equipmentSlotName) + ".Texture", textureStr);
                    continue;
                }
                configurationSection.set(makeFriendly(equipmentSlotName) + ".SkullType", "Default");
                UUID skullUUID = skullOwner.getUniqueId();
                configurationSection.set(makeFriendly(equipmentSlotName) + ".UUID", skullUUID.toString());
                ChatColorHandler.sendMessage(owner, Followers.configManager.getLangMessage("follower-default-skull"));
            } else if (currItem.getItemMeta() instanceof LeatherArmorMeta armorMeta) {
                Color armorColor = armorMeta.getColor();
                configurationSection.set(makeFriendly(equipmentSlotName) + ".Color", String.format("%02x%02x%02x", armorColor.getRed(), armorColor.getGreen(), armorColor.getBlue()));
            }
            if (currItem.getEnchantments().size() >= 1) {
                configurationSection.set(makeFriendly(equipmentSlotName) + ".Enchanted", "True");
            }
        }
        ChatColorHandler.sendMessage(owner, Followers.configManager.getLangMessage("follower-created").replaceAll("%follower%", followerName));
        saveFollowers();
        loadFollower(followerName);
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

    public void clearFollowerCache() {
        followerList.clear();
    }

    private File initYML() {
        File followerConfigFile = new File(plugin.getDataFolder(),"followers.yml");
        if (!followerConfigFile.exists()) {
            plugin.saveResource("followers.yml", false);
            plugin.getLogger().info("File Created: followers.yml");
        }
        return followerConfigFile;
    }

    private String makeFriendly(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
