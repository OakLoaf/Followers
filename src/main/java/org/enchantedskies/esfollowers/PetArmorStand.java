package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PetArmorStand {
    private final ESFollowers plugin;
    private final ArmorStand armorStand;

    public PetArmorStand(ESFollowers instance, Player owner, Location location, ItemStack headSlot, ItemStack chestSlot, ItemStack legsSlot, ItemStack feetSlot) {
        plugin = instance;
        armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
        armorStand.setInvulnerable(true);
        armorStand.setCanPickupItems(false);
        armorStand.setSmall(true);
        armorStand.getPersistentDataContainer().set(ESFollowers.petKey, PersistentDataType.STRING, owner.getUniqueId().toString());

        EntityEquipment armorEquipment = armorStand.getEquipment();
        if (armorEquipment != null) {
            if (headSlot != null) armorEquipment.setHelmet(headSlot);
            if (chestSlot != null) armorEquipment.setChestplate(chestSlot);
            if (legsSlot != null) armorEquipment.setLeggings(legsSlot);
            if (feetSlot != null) armorEquipment.setBoots(feetSlot);
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                armorStand.addEquipmentLock(equipmentSlot, lockType);
            }
        }
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public void startMovement(double speed) {
        String strUUID = armorStand.getPersistentDataContainer().get(ESFollowers.petKey, PersistentDataType.STRING);
        if (strUUID == null) return;
        Player player = Bukkit.getPlayer(UUID.fromString(strUUID));
        if (player == null) return;
        new BukkitRunnable() {
            public void run() {
                if (!armorStand.isValid()) {
                    cancel();
                    return;
                }
                Location petLoc = armorStand.getLocation();
                Vector difference = getDifference(player, armorStand);
                if (difference.clone().setY(0).lengthSquared() < 6.25) {
                    Vector differenceY = difference.clone().setX(0).setZ(0);
                    petLoc.add(differenceY.multiply(speed));
                } else {
                    Vector normalizedDifference = difference.clone().normalize();
                    petLoc.add(normalizedDifference.multiply(speed));
                }
                if (difference.lengthSquared() > 1024) {
                    armorStand.teleport(player);
                    return;
                }
                petLoc.setDirection(difference);
                armorStand.teleport(petLoc.add(0, getArmorStandYOffset(armorStand), 0));
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
