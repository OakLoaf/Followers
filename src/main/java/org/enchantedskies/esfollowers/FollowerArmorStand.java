package org.enchantedskies.esfollowers;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.enchantedskies.esfollowers.datamanager.FollowerHandler;

import java.util.UUID;

public class FollowerArmorStand {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private String followerName;
    private final ArmorStand armorStand;
    private NamespacedKey followerKey;

    public FollowerArmorStand(String followerName, Player owner, NamespacedKey followerKey) {
        this.followerKey = followerKey;
        this.followerName = followerName;

        armorStand = owner.getLocation().getWorld().spawn(owner.getLocation().add(-1.5, 0, 1.5), ArmorStand.class);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
        armorStand.setInvulnerable(true);
        armorStand.setCanPickupItems(false);
        armorStand.setSmall(true);
        armorStand.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, owner.getUniqueId().toString());

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            setFollowerArmorSlot(equipmentSlot, followerName);
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                armorStand.addEquipmentLock(equipmentSlot, lockType);
            }
        }
    }

    public FollowerArmorStand(String followerName, ArmorStand armorStand) {
        this.armorStand = armorStand;
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
                    ESFollowers.dataManager.removeFromPlayerFollowerMap(player.getUniqueId());
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
                    differenceY.setY(differenceY.getY() - 0.25);
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
        if (!ESFollowers.configManager.getFollowers().containsKey(followerName)) return;
        EntityEquipment armorEquipment = armorStand.getEquipment();
        if (armorEquipment == null) return;
        FollowerHandler follower = ESFollowers.configManager.getFollower(followerName);
        ItemStack item = new ItemStack(Material.AIR);
        switch (equipmentSlot) {
            case HEAD: item = follower.getHead(); break;
            case CHEST: item = follower.getChest(); break;
            case LEGS: item = follower.getLegs(); break;
            case FEET: item = follower.getFeet(); break;
            case HAND: item = follower.getMainHand(); break;
            case OFF_HAND: item = follower.getOffHand(); break;
        }
        armorEquipment.setItem(equipmentSlot, item);
    }

    private double getArmorStandYOffset(ArmorStand armorStand) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Bukkit.getCurrentTick() + armorStand.getEntityId()));
    }

    private double getPitch(Player player, ArmorStand armorStand) {
        Vector difference = (player.getEyeLocation().subtract(0,0.6, 0)).subtract(armorStand.getEyeLocation()).toVector();
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) return (float)(difference.getY() > 0.0D ? -90 : 90);
        else return Math.atan(-difference.getY() / Math.sqrt((difference.getX()*difference.getX()) + (difference.getZ()*difference.getZ())));
    }

    private Vector getDifference(Player player, ArmorStand armorStand) {
        return player.getEyeLocation().subtract(armorStand.getEyeLocation()).toVector();
    }

    private double eulerToDegree(double euler) {
        return (euler / (2 * Math.PI)) * 360;
    }
}
