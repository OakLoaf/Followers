package org.enchantedskies.esfollowers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FollowerCreator implements Listener {
    private final ESFollowers plugin;
    private final FileConfiguration config;
    private final HashMap<String, ItemStack> followerSkullMap;

    public FollowerCreator(ESFollowers instance, HashMap<String, ItemStack> followerSkullMap) {
        plugin = instance;
        config = plugin.getConfig();
        this.followerSkullMap = followerSkullMap;
    }

    public ItemStack getCreator() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("Follower Creator");
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
        return item;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        if (!heldItem.isSimilar(getCreator())) return;
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
                if (skullTexture != null || skullTexture.equalsIgnoreCase("error")) item = getCustomSkull(skullTexture);
                followerSkullMap.put(armorStandName, item);
            } else {
                String skullUUID = configSection.getString("UUID");
                if (skullUUID == null) {
                    followerSkullMap.put(armorStandName, new ItemStack(Material.PLAYER_HEAD));
                    return;
                }
                getPlayerSkull(UUID.fromString(skullUUID)).thenAccept(itemStack -> Bukkit.getScheduler().runTask(plugin, runnable -> { followerSkullMap.put(armorStandName, itemStack); }));
            }
        }
    }

    @EventHandler
    public void onPlayerManipulateArmorStand(PlayerArmorStandManipulateEvent event) {
        ItemStack item = event.getPlayerItem();
        if (!item.isSimilar(getCreator())) return;
        event.setCancelled(true);
    }

    private CompletableFuture<ItemStack> getPlayerSkull(UUID uuid) {
        CompletableFuture<ItemStack> futureItemStack = new CompletableFuture<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
                PlayerProfile playerProfile = Bukkit.createProfile(uuid);
                playerProfile.complete();
                skullMeta.setPlayerProfile(playerProfile);
                skullItem.setItemMeta(skullMeta);
                futureItemStack.complete(skullItem);
            }
        }.runTaskAsynchronously(plugin);
        return futureItemStack;
    }

    private ItemStack getCustomSkull(String texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
        Set<ProfileProperty> profileProperties = playerProfile.getProperties();
        profileProperties.add(new ProfileProperty("textures", texture));
        playerProfile.setProperties(profileProperties);
        skullMeta.setPlayerProfile(playerProfile);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    private String makeFriendly(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}