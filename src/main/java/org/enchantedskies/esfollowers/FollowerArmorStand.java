package org.enchantedskies.esfollowers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.UUID;

public class FollowerArmorStand {
    private final ESFollowers plugin;
    private final ArmorStand armorStand;

    public FollowerArmorStand(ESFollowers instance, String followerName, Player owner) {
        plugin = instance;
        armorStand = owner.getLocation().getWorld().spawn(owner.getLocation(), ArmorStand.class);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
        armorStand.setInvulnerable(true);
        armorStand.setCanPickupItems(false);
        armorStand.setSmall(true);
        armorStand.getPersistentDataContainer().set(ESFollowers.followerKey, PersistentDataType.STRING, owner.getUniqueId().toString());

//        EntityEquipment armorEquipment = armorStand.getEquipment();
//        if (armorEquipment != null) {
//            if (headSlot != null) armorEquipment.setHelmet(headSlot);
//            if (chestSlot != null) armorEquipment.setChestplate(chestSlot);
//            if (legsSlot != null) armorEquipment.setLeggings(legsSlot);
//            if (feetSlot != null) armorEquipment.setBoots(feetSlot);
//        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                armorStand.addEquipmentLock(equipmentSlot, lockType);
            }
        }
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public void setHeadSlot( ItemStack item) {
        EntityEquipment followerEquipment = armorStand.getEquipment();
        if (followerEquipment == null ) return;
        followerEquipment.setHelmet(item);
    }

    public void setChestSlot( ItemStack item) {
        EntityEquipment followerEquipment = armorStand.getEquipment();
        if (followerEquipment == null ) return;
        followerEquipment.setChestplate(item);
    }

    public void setLegsSlot( ItemStack item) {
        EntityEquipment followerEquipment = armorStand.getEquipment();
        if (followerEquipment == null ) return;
        followerEquipment.setLeggings(item);
    }

    public void setFeetSlot( ItemStack item) {
        EntityEquipment followerEquipment = armorStand.getEquipment();
        if (followerEquipment == null ) return;
        followerEquipment.setBoots(item);
    }

    public void startMovement(double speed) {
        String strUUID = armorStand.getPersistentDataContainer().get(ESFollowers.followerKey, PersistentDataType.STRING);
        if (strUUID == null) return;
        Player player = Bukkit.getPlayer(UUID.fromString(strUUID));
        if (player == null) return;
        new BukkitRunnable() {
            public void run() {
                if (!armorStand.isValid()) {
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
                    followerLoc.add(differenceY.multiply(speed));
                } else {
                    Vector normalizedDifference = difference.clone().normalize();
                    followerLoc.add(normalizedDifference.multiply(speed));
                }
                if (difference.lengthSquared() > 1024) {
                    armorStand.teleport(player);
                    return;
                }
                followerLoc.setDirection(difference);
                armorStand.teleport(followerLoc.add(0, getArmorStandYOffset(armorStand), 0));
                double headPoseX = eulerToDegree(armorStand.getHeadPose().getX());
                EulerAngle newHeadPoseX = new EulerAngle(getPitch(player, armorStand), 0, 0);
                if (headPoseX > 60 && headPoseX < 290) {
                    if (headPoseX <= 175) {
                        newHeadPoseX.setX(60D);
                    } else {
                        newHeadPoseX.setX(290D);
                    }
                }
                armorStand.setHeadPose(newHeadPoseX);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }


    // Private functions

    private ItemStack getItemStack(Material material) {
        return new ItemStack(material);
    }

    private ItemStack getPlayerSkull(UUID uuid) {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        if (skullMeta == null) return skullItem;
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    private ItemStack getCustomSkull(String texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setPlayerProfile(Bukkit.createProfile(UUID.randomUUID(), null));
        PlayerProfile playerProfile = skullMeta.getPlayerProfile();
        if (playerProfile == null) return skull;
        playerProfile.getProperties().add(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(playerProfile);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    private ItemStack getColouredArmour(Material material, String hexColour) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta)) return item;
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) item.getItemMeta();
        int red = Integer.valueOf(hexColour.substring(1, 3), 16);
        int green = Integer.valueOf(hexColour.substring(3, 5), 16);
        int blue = Integer.valueOf(hexColour.substring(5, 7), 16);
        armorMeta.setColor(Color.fromRGB(red, green, blue));
        item.setItemMeta(armorMeta);
        return item;
    }

    private double getArmorStandYOffset(ArmorStand armorStand) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Bukkit.getCurrentTick() + armorStand.getEntityId()));
    }

    private double getPitch(Player player, ArmorStand armorStand) {
        Vector difference = (player.getEyeLocation().subtract(0,0.4, 0)).subtract(armorStand.getEyeLocation()).toVector();
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
