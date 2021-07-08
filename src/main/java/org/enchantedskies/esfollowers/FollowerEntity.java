package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.enchantedskies.esfollowers.datamanager.FollowerUser;

import java.util.UUID;

public class FollowerEntity {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private final NamespacedKey followerKey = new NamespacedKey(ESFollowers.getInstance(), "ESFollower");
    private final Player owner;
    private final ArmorStand followerAS;
    private ArmorStand nameTagAS;
    private String follower;
    private boolean isPlayerInvisible;

    public FollowerEntity(Player owner, String follower) {
        this.owner = owner;
        this.follower = follower;
        this.isPlayerInvisible = owner.isInvisible();
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(owner.getUniqueId());
        ESFollowers.dataManager.putInPlayerFollowerMap(owner.getUniqueId(), this);
        followerUser.setFollowerEnabled(true);

        followerAS = owner.getLocation().getWorld().spawn(owner.getLocation().add(-1.5, 0, 1.5), ArmorStand.class, (armorStand -> {
            try {
                armorStand.setBasePlate(false);
                armorStand.setArms(true);
                armorStand.setInvulnerable(true);
                armorStand.setCanPickupItems(false);
                armorStand.setSmall(true);
                armorStand.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }));
        ESFollowers.dataManager.setActiveArmorStand(followerAS.getUniqueId());

        setVisible(!owner.isInvisible());

        if (!ESFollowers.configManager.areHitboxesEnabled()) {
            followerAS.setMarker(true);
        }

        if (followerUser.isDisplayNameEnabled()) {
            if (ESFollowers.configManager.areHitboxesEnabled()) {
                followerAS.setCustomName(followerUser.getDisplayName());
                followerAS.setCustomNameVisible(followerUser.isDisplayNameEnabled());
            } else if (nameTagAS != null) {
                nameTagAS.setCustomName(followerUser.getDisplayName());
                nameTagAS.setCustomNameVisible(followerUser.isDisplayNameEnabled());
            }
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                followerAS.addEquipmentLock(equipmentSlot, lockType);
            }
        }
        setFollower(follower);
        startMovement(ESFollowers.configManager.getSpeed());
    }

    public ArmorStand getArmorStand() {
        return followerAS;
    }

    public void setFollower(String newFollower) {
        this.follower = newFollower;
        ESFollowers.dataManager.getFollowerUser(owner.getUniqueId()).setFollower(newFollower);
        if (!owner.isInvisible()) reloadInventory();
    }

    public void setDisplayNameVisible(boolean visible) {
        ESFollowers.dataManager.getFollowerUser(owner.getUniqueId()).setDisplayNameEnabled(visible);
        if (!owner.isInvisible()) {
            if (ESFollowers.configManager.areHitboxesEnabled()) followerAS.setCustomNameVisible(visible);
            else displayNametag(visible);
        }
    }

    public void setDisplayName(String newName) {
        ESFollowers.dataManager.getFollowerUser(owner.getUniqueId()).setDisplayName(newName);
        setDisplayNameVisible(true);
        if (ESFollowers.configManager.areHitboxesEnabled()) followerAS.setCustomName(newName);
        else nameTagAS.setCustomName(newName);
    }

    public void setVisible(boolean visible) {
        followerAS.setVisible(visible);
        if (!ESFollowers.configManager.areHitboxesEnabled() && ESFollowers.dataManager.getFollowerUser(owner.getUniqueId()).isDisplayNameEnabled()) displayNametag(visible);
        if (visible) reloadInventory();
        else clearInventory();
    }

    public void clearInventory() {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            followerAS.setItem(equipmentSlot, new ItemStack(Material.AIR));
        }
    }

    public void reloadInventory() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (owner.isInvisible()) return;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                setFollowerArmorSlot(equipmentSlot, follower);
            }
        }, 1);
    }

    public void setFollowerArmorSlot(EquipmentSlot equipmentSlot, String followerName) {
        if (!ESFollowers.followerManager.getFollowers().containsKey(followerName)) return;
        EntityEquipment armorEquipment = followerAS.getEquipment();
        if (armorEquipment == null) return;
        FollowerHandler follower = ESFollowers.followerManager.getFollower(followerName);
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

    public void disable() {
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(owner.getUniqueId());
        if (followerUser != null) followerUser.setFollowerEnabled(false);
        kill();
    }

    public void kill() {
        if (followerAS == null) return;
        ESFollowers.dataManager.setActiveArmorStand(followerAS.getUniqueId(), false);
        ESFollowers.dataManager.removeFromPlayerFollowerMap(owner.getUniqueId());
        followerAS.remove();
        if (nameTagAS != null) nameTagAS.remove();
    }


    //////////////////////////////
    //    Movement Functions    //
    //////////////////////////////

    private void startMovement(double speed) {
        String strUUID = followerAS.getPersistentDataContainer().get(followerKey, PersistentDataType.STRING);
        if (strUUID == null) return;
        Player player = Bukkit.getPlayer(UUID.fromString(strUUID));
        if (player == null) return;
        new BukkitRunnable() {
            public void run() {
                if (!followerAS.isValid()) {
                    kill();
                    cancel();
                    return;
                }
                if (followerAS.getWorld() != player.getWorld()) {
                    teleportArmorStands(player.getLocation().add(1.5, 0, 1.5));
                    return;
                }
                if (isPlayerInvisible != player.isInvisible()) {
                    setVisible(!player.isInvisible());
                    isPlayerInvisible = player.isInvisible();
                }
                Location followerLoc = followerAS.getLocation();
                Vector difference = getDifference(player, followerAS);
                if (difference.clone().setY(0).lengthSquared() < 6.25) {
                    Vector differenceY = difference.clone().setX(0).setZ(0);
                    if (ESFollowers.configManager.areHitboxesEnabled()) differenceY.setY(differenceY.getY() - 0.25);
                    else differenceY.setY(differenceY.getY() - 0.7);
                    followerLoc.add(differenceY.multiply(speed));
                } else {
                    Vector normalizedDifference = difference.clone().normalize();
                    double distance = difference.length() - 5;
                    if (distance < 1) distance = 1;
                    followerLoc.add(normalizedDifference.multiply(speed * distance));
                }
                if (difference.lengthSquared() > 1024) {
                    teleportArmorStands(player.getLocation().add(1.5, 0, 1.5));
                    return;
                }
                followerLoc.setDirection(difference);
                teleportArmorStands(followerLoc.add(0, getArmorStandYOffset(followerAS), 0));
                if (Bukkit.getCurrentTick() % 2 != 0) return;
                double headPoseX = eulerToDegree(followerAS.getHeadPose().getX());
                EulerAngle newHeadPoseX = new EulerAngle(getPitch(player, followerAS), 0, 0);
                if (headPoseX > 60 && headPoseX < 290) {
                    if (headPoseX <= 175) newHeadPoseX.setX(60D);
                    else newHeadPoseX.setX(290D);
                }
                followerAS.setHeadPose(newHeadPoseX);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void displayNametag(boolean isDisplayed) {
        if (isDisplayed) {
            if (nameTagAS == null) {
                nameTagAS = owner.getLocation().getWorld().spawn(followerAS.getLocation().add(0, 1, 0), ArmorStand.class, (armorStand -> {
                    armorStand.setInvulnerable(true);
                    armorStand.setVisible(false);
                    armorStand.setMarker(true);
                    armorStand.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, "");
                }));
                ESFollowers.dataManager.setActiveArmorStand(nameTagAS.getUniqueId());
            }
            nameTagAS.setCustomName(ESFollowers.dataManager.getFollowerUser(owner.getUniqueId()).getDisplayName());
            nameTagAS.setCustomNameVisible(true);
        } else {
            ESFollowers.dataManager.setActiveArmorStand(nameTagAS.getUniqueId(), false);
            if (nameTagAS != null) nameTagAS.remove();
            nameTagAS = null;
        }
    }

    private void teleportArmorStands(Location location) {
        followerAS.teleport(location);
        if (nameTagAS != null) nameTagAS.teleport(location.add(0, 1, 0));
    }

    private double getArmorStandYOffset(ArmorStand armorStand) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Bukkit.getCurrentTick() + armorStand.getEntityId()));
    }

    private double getPitch(Player player, ArmorStand armorStand) {
        Vector difference = (player.getEyeLocation().subtract(0,0.9, 0)).subtract(armorStand.getEyeLocation()).toVector();
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
