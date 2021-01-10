package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.UUID;

public class FollowerCreator implements Listener {
    private final ESFollowers plugin;
    private final FileConfiguration config;
    private final HashMap<String, ItemStack> followerSkullMap;
    private final ItemStack creatorItem;

    public FollowerCreator(ESFollowers instance, HashMap<String, ItemStack> followerSkullMap) {
        plugin = instance;
        config = plugin.getConfig();
        this.followerSkullMap = followerSkullMap;

        creatorItem = new ItemStack(Material.STICK);
        ItemMeta creatorMeta = creatorItem.getItemMeta();
        creatorMeta.setDisplayName("Follower Creator");
        creatorMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        creatorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        creatorItem.setItemMeta(creatorMeta);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        if (!heldItem.isSimilar(creatorItem)) return;
        event.setCancelled(true);
        if (!player.hasPermission("followers.admin.create")) {
            player.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
            return;
        }
        String armorStandName = armorStand.getCustomName();
        if (armorStandName == null) {
            player.sendMessage("§8§l[§d§lES§8§l] §7This ArmorStand does not have a name, unable to register as a Follower.");
            return;
        }
        ConfigurationSection configurationSection = config.getConfigurationSection(armorStandName);
        if (configurationSection != null) {
            player.sendMessage("§8§l[§d§lES§8§l] §7A Follower already exists with this name.");
            return;
        }
        configurationSection = config.createSection(armorStandName);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack currItem = armorStand.getItem(equipmentSlot);
            Material material = currItem.getType();
            if (material == Material.AIR) continue;
            configurationSection.set(makeFriendly(equipmentSlot.name()) + ".Material", material.toString().toLowerCase());
            if (currItem.getType() == Material.PLAYER_HEAD) {
                SkullMeta skullMeta = (SkullMeta) currItem.getItemMeta();
                OfflinePlayer skullOwner = skullMeta.getOwningPlayer();
                if (skullOwner == null) {
                    configurationSection.set(makeFriendly(equipmentSlot.name()) + ".SkullType", "Texture");
                    player.sendMessage("§8§l[§d§lES§8§l] §7Could not find the owner of the skull in the §c" + makeFriendly(equipmentSlot.name()) + " §7slot, added Custom player head to config.yml file with no texture.");
                    configurationSection.set(makeFriendly(equipmentSlot.name()) + ".Texture", "error");
                    continue;
                }
                configurationSection.set(makeFriendly(equipmentSlot.name()) + ".SkullType", "Default");
                UUID skullUUID = skullOwner.getUniqueId();
                configurationSection.set(makeFriendly(equipmentSlot.name()) + ".UUID", skullUUID.toString());
                player.sendMessage("§8§l[§d§lES§8§l] §7Skull has been created as Default SkullType. To get custom textures manually edit the config.");
            } else if (currItem.getItemMeta() instanceof LeatherArmorMeta) {
                LeatherArmorMeta armorMeta = (LeatherArmorMeta) currItem.getItemMeta();
                Color armorColor = armorMeta.getColor();
                configurationSection.set(makeFriendly(equipmentSlot.name()) + ".Color", String.format("%02x%02x%02x", armorColor.getRed(), armorColor.getGreen(), armorColor.getBlue()));
            }
        }
        player.sendMessage("§8§l[§d§lES§8§l] §7A Follower has been added with the name §a" + armorStandName);
        plugin.saveConfig();
        ConfigurationSection configSection = config.getConfigurationSection(armorStandName + ".Head");
        if (configSection == null) return;
        String materialStr = configSection.getString("Material", "");
        Material material = Material.getMaterial(materialStr.toUpperCase());
        if (material == null) return;
        ItemStack item = new ItemStack(material);
        if (material == Material.PLAYER_HEAD) {
            String skullType = configSection.getString("SkullType", "");
            if (skullType.equalsIgnoreCase("custom")) {
                String skullTexture = configSection.getString("Texture");
                if (skullTexture != null || skullTexture.equalsIgnoreCase("error")) item = ESFollowers.skullCreator.getCustomSkull(skullTexture);
                followerSkullMap.put(armorStandName, item);
            } else {
                String skullUUID = configSection.getString("UUID");
                if (skullUUID == null) {
                    followerSkullMap.put(armorStandName, new ItemStack(Material.PLAYER_HEAD));
                    return;
                }
                ESFollowers.skullCreator.getPlayerSkull(UUID.fromString(skullUUID), plugin).thenAccept(itemStack -> Bukkit.getScheduler().runTask(plugin, runnable -> { followerSkullMap.put(armorStandName, itemStack); }));
            }
        }
    }

    @EventHandler
    public void onPlayerManipulateArmorStand(PlayerArmorStandManipulateEvent event) {
        ItemStack item = event.getPlayerItem();
        if (!item.isSimilar(creatorItem)) return;
        event.setCancelled(true);
    }

    public ItemStack getCreatorItem() {
        return creatorItem;
    }

    private String makeFriendly(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}