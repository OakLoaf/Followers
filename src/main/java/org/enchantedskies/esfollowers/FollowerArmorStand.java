package org.enchantedskies.esfollowers;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class FollowerArmorStand {
    private final ESFollowers plugin;
    private final ArmorStand armorStand;
    private final FileConfiguration config;
    private final HashMap<String, ItemStack> followerSkullMap;
    private NamespacedKey followerKey;
    private HashMap<UUID, UUID> playerFollowerMap;
    private String followerName;

    public FollowerArmorStand(ESFollowers instance, String followerName, Player owner, HashMap<String, ItemStack> followerSkullMap, HashMap<UUID, UUID> playerFollowerMap, NamespacedKey followerKey) {
        plugin = instance;
        config = plugin.getConfig();
        this.followerKey = followerKey;
        this.followerName = followerName;
        this.followerSkullMap = followerSkullMap;
        this.playerFollowerMap = playerFollowerMap;

        armorStand = owner.getLocation().getWorld().spawn(owner.getLocation().add(-1.5, 0, 1.5), ArmorStand.class);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
        armorStand.setInvulnerable(true);
        armorStand.setCanPickupItems(false);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        armorStand.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, owner.getUniqueId().toString());

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            setFollowerArmorSlot(equipmentSlot, followerName);
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                armorStand.addEquipmentLock(equipmentSlot, lockType);
            }
        }
    }

    public FollowerArmorStand(ESFollowers instance, String followerName, ArmorStand armorStand, HashMap<String, ItemStack> followerSkullMap) {
        plugin = instance;
        config = plugin.getConfig();
        this.armorStand = armorStand;
        this.followerSkullMap = followerSkullMap;
        changeFollower(followerName);
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public void changeFollower(String newFollowerName) {
        this.followerName = newFollowerName;
        reloadInventory();
    }

    public void reloadInventory() {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            setFollowerArmorSlot(equipmentSlot, followerName);
        }
    }

    public void startMovement(double speed) {
        String strUUID = armorStand.getPersistentDataContainer().get(followerKey, PersistentDataType.STRING);
        if (strUUID == null) return;
        Player player = Bukkit.getPlayer(UUID.fromString(strUUID));
        if (player == null) return;
        new BukkitRunnable() {
            public void run() {
                if (!armorStand.isValid()) {
                    playerFollowerMap.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                if (armorStand.getWorld() != player.getWorld()) {
                    armorStand.teleport(player);
                    return;
                }
                Location followerLoc = armorStand.getLocation();
                Vector difference = getDifference(player, armorStand);
                if (difference.clone().setY(0).lengthSquared() < 6.25) {
                    Vector differenceY = difference.clone().setX(0).setZ(0);
                    differenceY.setY(differenceY.getY() - 0.5);
                    followerLoc.add(differenceY.multiply(speed));
                } else {
                    Vector normalizedDifference = difference.clone().normalize();
                    double distance = difference.length() - 5;
                    if (distance < 1) distance = 1;
                    followerLoc.add(normalizedDifference.multiply(speed * distance));
                }
                if (difference.lengthSquared() > 1024) {
                    armorStand.teleport(player);
                    return;
                }
                followerLoc.setDirection(difference);
                armorStand.teleport(followerLoc.add(0, getArmorStandYOffset(armorStand), 0));
                if (Bukkit.getCurrentTick() % 2 != 0) return;
                double headPoseX = eulerToDegree(armorStand.getHeadPose().getX());
                EulerAngle newHeadPoseX = new EulerAngle(getPitch(player, armorStand), 0, 0);
                if (headPoseX > 60 && headPoseX < 290) {
                    if (headPoseX <= 175) newHeadPoseX.setX(60D);
                    else newHeadPoseX.setX(290D);
                }
                armorStand.setHeadPose(newHeadPoseX);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }


    // Private functions

    private void setFollowerArmorSlot(EquipmentSlot equipmentSlot, String followerName) {
        boolean foundName = false;
        for (String currName : config.getKeys(false)) {
            if (followerName.equalsIgnoreCase(currName)) {
                followerName = currName;
                foundName = true;
                break;
            }
        }
        if (!foundName) return;
        EntityEquipment armorEquipment = armorStand.getEquipment();
        if (armorEquipment == null) return;
        ConfigurationSection configSection = config.getConfigurationSection(followerName + "." + makeFriendly(equipmentSlot.name()));
        if (configSection == null) {
            armorEquipment.setItem(equipmentSlot, new ItemStack(Material.AIR));
            return;
        }
        String materialStr = configSection.getString("Material", "");
        Material material = Material.getMaterial(materialStr.toUpperCase());
        if (material == null) return;
        ItemStack item = new ItemStack(material);
        if (material == Material.PLAYER_HEAD) item = followerSkullMap.get(followerName);
        else if (item.getItemMeta() instanceof LeatherArmorMeta) {
            String color = configSection.getString("Color");
            item = getColouredArmour(material, color);
            armorEquipment.setItem(equipmentSlot, item);
            return;
        }
        armorEquipment.setItem(equipmentSlot, item);
    }

    private String makeFriendly(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    private ItemStack getColouredArmour(Material material, String hexColour) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta)) return item;
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) item.getItemMeta();
        int red = Integer.valueOf(hexColour.substring(0, 2), 16);
        int green = Integer.valueOf(hexColour.substring(2, 4), 16);
        int blue = Integer.valueOf(hexColour.substring(4, 6), 16);
        armorMeta.setColor(Color.fromRGB(red, green, blue));
        item.setItemMeta(armorMeta);
        return item;
    }

    private double getArmorStandYOffset(ArmorStand armorStand) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Bukkit.getCurrentTick() + armorStand.getEntityId()));
    }

    private double getPitch(Player player, ArmorStand armorStand) {
        Vector difference = (player.getEyeLocation().subtract(0,0.9, 0)).subtract(armorStand.getEyeLocation()).toVector();
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) {
            return (float)(difference.getY() > 0.0D ? -90 : 90);
        } else {
            return Math.atan(-difference.getY() / Math.sqrt((difference.getX()*difference.getX()) + (difference.getZ()*difference.getZ())));
        }
    }

    private Vector getDifference(Player player, ArmorStand armorStand) {
        return player.getEyeLocation().subtract(armorStand.getEyeLocation()).toVector();
    }

    private double eulerToDegree(double euler) {
        return (euler / (2 * Math.PI)) * 360;
    }
}
