package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.enchantedskies.esfollowers.ESFollowers;

import java.util.HashMap;
import java.util.UUID;

public class ConfigManager {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private FileConfiguration config;
    private final HashMap<String, FollowerHandler> followerList = new HashMap<>();

    public ConfigManager() {
        config = plugin.getConfig();
        for (String followerName : config.getKeys(false)) {
            if (followerName.equals("Database")) continue;
            ConfigurationSection configSection = config.getConfigurationSection(followerName);
            followerList.put(followerName, new FollowerHandler(configSection));
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getDatabaseType() {
        return config.getString("Database.type");
    }

    public void saveConfig() {
        plugin.saveConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        clearFollowerCache();
        for (String followerName : config.getKeys(false)) {
            if (followerName.equals("Database")) continue;
            loadFollower(followerName);
        }
    }

    public void createFollower(Player owner, String followerName, ArmorStand armorStand) {
        FileConfiguration config = getConfig();
        ConfigurationSection configurationSection = config.getConfigurationSection(followerName);
        if (configurationSection != null) {
            owner.sendMessage(ESFollowers.prefix + "§7A Follower already exists with this name.");
            return;
        }
        configurationSection = config.createSection(followerName);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack currItem = armorStand.getItem(equipmentSlot);
            Material material = currItem.getType();
            if (material == Material.AIR) continue;
            String equipmentSlotName = makeFriendly(equipmentSlot.name());
            switch (equipmentSlotName) {
                case "Hand": equipmentSlotName = "MainHand"; break;
                case "Off_hand": equipmentSlotName = "OffHand"; break;
            }
            configurationSection.set(equipmentSlotName + ".Material", material.toString().toLowerCase());
            if (currItem.getType() == Material.PLAYER_HEAD) {
                SkullMeta skullMeta = (SkullMeta) currItem.getItemMeta();
                OfflinePlayer skullOwner = skullMeta.getOwningPlayer();
                if (skullOwner == null) {
                    configurationSection.set(makeFriendly(equipmentSlot.name()) + ".SkullType", "Custom");
                    owner.sendMessage(ESFollowers.prefix + "§7Could not find the owner of the skull in the §c" + makeFriendly(equipmentSlot.name()) + " §7slot, added Custom player head to config.yml file with no texture.");
                    configurationSection.set(makeFriendly(equipmentSlot.name()) + ".Texture", "error");
                    continue;
                }
                configurationSection.set(makeFriendly(equipmentSlot.name()) + ".SkullType", "Default");
                UUID skullUUID = skullOwner.getUniqueId();
                configurationSection.set(makeFriendly(equipmentSlot.name()) + ".UUID", skullUUID.toString());
                owner.sendMessage(ESFollowers.prefix + "§7Skull has been created as Default SkullType. To get custom textures manually edit the config.");
            } else if (currItem.getItemMeta() instanceof LeatherArmorMeta) {
                LeatherArmorMeta armorMeta = (LeatherArmorMeta) currItem.getItemMeta();
                Color armorColor = armorMeta.getColor();
                configurationSection.set(makeFriendly(equipmentSlot.name()) + ".Color", String.format("%02x%02x%02x", armorColor.getRed(), armorColor.getGreen(), armorColor.getBlue()));
            }
            if (currItem.getEnchantments().size() >= 1) {
                configurationSection.set(makeFriendly(equipmentSlot.name()) + ".Enchanted", "True");
            }
        }
        owner.sendMessage(ESFollowers.prefix + "§7A Follower has been added with the name §a" + followerName);
        saveConfig();
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
        saveConfig();
    }

    public FollowerHandler getFollower(String followerName) {
        return followerList.get(followerName);
    }

    public HashMap<String, FollowerHandler> getFollowers() {
        return followerList;
    }

    public void clearFollowerCache() {
        followerList.clear();
    }

    private String makeFriendly(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
