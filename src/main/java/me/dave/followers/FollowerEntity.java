package me.dave.followers;

import me.dave.chatcolorhandler.ChatColorHandler;
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
import me.dave.followers.datamanager.FollowerHandler;
import me.dave.followers.datamanager.FollowerUser;

import java.util.UUID;

public class FollowerEntity {
    private final Followers plugin = Followers.getInstance();
    private final NamespacedKey followerKey = new NamespacedKey(Followers.getInstance(), "Follower");
    private final Player owner;
    private ArmorStand followerAS;
    private ArmorStand nameTagAS;
    private String follower;
    private boolean isPlayerInvisible;
    private String poseName;
    private boolean isEnabled;

    private final EulerAngle defaultLeftArm = new EulerAngle(-0.17453292519943295, 0, -0.17453292519943295);
    private final EulerAngle defaultRightArm = new EulerAngle(-0.2617993877991494, 0, 0.17453292519943295);
    private final EulerAngle defaultLeftLeg = new EulerAngle(-0.017453292519943295, 0, -0.017453292519943295);
    private final EulerAngle defaultRightLeg = new EulerAngle(0.017453292519943295, 0, 0.017453292519943295);

    private final EulerAngle sittingLeftArm = new EulerAngle(-0.78, 0, -0.17453292519943295);
    private final EulerAngle sittingRightArm = new EulerAngle(-0.78, 0, 0.17453292519943295);
    private final EulerAngle sittingLeftLeg = new EulerAngle(4.6, -0.222, 0);
    private final EulerAngle sittingRightLeg = new EulerAngle(4.6, 0.222, 0);

    private final EulerAngle spinningLeftArm = new EulerAngle(-0.17453292519943295, 0, -0.17453292519943295);
    private final EulerAngle spinningRightArm = new EulerAngle(-0.2617993877991494, 0, 0.17453292519943295);
    private final EulerAngle spinningLeftLeg = new EulerAngle(-0.017453292519943295, 0, -0.017453292519943295);
    private final EulerAngle spinningRightLeg = new EulerAngle(0.017453292519943295, 0, 0.017453292519943295);



    public FollowerEntity(Player owner, String follower) {
        this.owner = owner;
        this.follower = follower;
        this.isPlayerInvisible = owner.isInvisible();

        spawn();
    }

    public void setFollower(String newFollower) {
        this.follower = newFollower;

        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setFollower(newFollower);
        if (!owner.isInvisible()) reloadInventory();
    }

    public void setDisplayNameVisible(boolean visible) {
        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setDisplayNameEnabled(visible);
        if (!owner.isInvisible()) {
            if (Followers.configManager.areHitboxesEnabled()) followerAS.setCustomNameVisible(visible);
            else displayNametag(visible);
        }
    }

    public void setDisplayName(String newName) {
        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setDisplayName(newName);
        setDisplayNameVisible(true);
        if (Followers.configManager.areHitboxesEnabled()) followerAS.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
        else nameTagAS.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
    }

    public void setVisible(boolean visible) {
        FollowerHandler followerConfig = Followers.followerManager.getFollower(follower);
        if (followerConfig == null) return;
        followerAS.setVisible(followerConfig.isVisible() && visible);
        if (!Followers.configManager.areHitboxesEnabled() && Followers.dataManager.getFollowerUser(owner.getUniqueId()).isDisplayNameEnabled()) displayNametag(visible);
        if (visible) reloadInventory();
        else clearInventory();
    }

    public void clearInventory() {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            followerAS.getEquipment().setItem(equipmentSlot, new ItemStack(Material.AIR));
        }
    }

    public void reloadInventory() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (owner.isInvisible()) return;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                setFollowerArmorSlot(equipmentSlot, follower);
            }
            FollowerHandler followerEntity = Followers.followerManager.getFollower(follower);
            if (followerEntity == null) return;
            followerAS.setVisible(followerEntity.isVisible());
        }, 1);
    }

    public void setFollowerArmorSlot(EquipmentSlot equipmentSlot, String followerName) {
        if (!Followers.followerManager.getFollowers().containsKey(followerName)) return;
        EntityEquipment armorEquipment = followerAS.getEquipment();
        if (armorEquipment == null) return;
        FollowerHandler follower = Followers.followerManager.getFollower(followerName);
        new ItemStack(Material.AIR);
        ItemStack item = switch (equipmentSlot) {
            case HEAD -> follower.getHead();
            case CHEST -> follower.getChest();
            case LEGS -> follower.getLegs();
            case FEET -> follower.getFeet();
            case HAND -> follower.getMainHand();
            case OFF_HAND -> follower.getOffHand();
        };
        armorEquipment.setItem(equipmentSlot, item);
    }

    public void disable() {
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(owner.getUniqueId());
        if (followerUser != null) followerUser.setFollowerEnabled(false);
        isEnabled = false;
        kill(false);
    }

    public void spawn() {
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(owner.getUniqueId());
        Followers.dataManager.putInPlayerFollowerMap(owner.getUniqueId(), this);
        if (followerUser != null) followerUser.setFollowerEnabled(true);
        isEnabled = true;

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

        Followers.dataManager.setActiveArmorStand(followerAS.getUniqueId());

        setVisible(!owner.isInvisible());
        if (!Followers.configManager.areHitboxesEnabled()) followerAS.setMarker(true);

        if (followerUser.isDisplayNameEnabled()) {
            if (Followers.configManager.areHitboxesEnabled()) {
                followerAS.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", followerUser.getDisplayName())));
                followerAS.setCustomNameVisible(followerUser.isDisplayNameEnabled());
            } else if (nameTagAS != null) {
                nameTagAS.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", followerUser.getDisplayName())));
                nameTagAS.setCustomNameVisible(followerUser.isDisplayNameEnabled());
            }
        }

        setFollower(follower);
        startMovement(Followers.configManager.getSpeed());
    }

    public void kill(boolean respawn) {
        if (followerAS == null) return;

        Followers.dataManager.setActiveArmorStand(followerAS.getUniqueId(), false);

        Followers.dataManager.removeFromPlayerFollowerMap(owner.getUniqueId());
        followerAS.remove();
        if (nameTagAS != null) nameTagAS.remove();

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(owner.getUniqueId());
        if (followerUser != null && owner.isOnline()) followerUser.setFollowerEnabled(false);
        isEnabled = false;

        if (respawn) spawn();
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
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        kill(isEnabled);
                    }, 5);
                    cancel();
                    return;
                }
                if (followerAS.getWorld() != player.getWorld()) {
                    teleportToPlayer(player);
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
                    if (Followers.configManager.areHitboxesEnabled()) differenceY.setY(differenceY.getY() - 0.25);
                    else differenceY.setY(differenceY.getY() - 0.7);
                    followerLoc.add(differenceY.multiply(speed));
                } else {
                    Vector normalizedDifference = difference.clone().normalize();
                    double distance = difference.length() - 5;
                    if (distance < 1) distance = 1;
                    followerLoc.add(normalizedDifference.multiply(speed * distance));
                }
                if (difference.lengthSquared() > 1024) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> teleportToPlayer(player), 5);
                    return;
                }
                followerLoc.setDirection(difference);
                teleportArmorStands(followerLoc.add(0, getArmorStandYOffset(followerAS), 0));
                if (Followers.getCurrentTick() % 2 != 0) return;
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

    public void setPose(String poseName) {
        if (poseName.equalsIgnoreCase(this.poseName)) return;
        this.poseName = poseName;

        switch (poseName) {
            case "default" -> {
                followerAS.setLeftArmPose(defaultLeftArm);
                followerAS.setRightArmPose(defaultRightArm);
                followerAS.setLeftLegPose(defaultLeftLeg);
                followerAS.setRightLegPose(defaultRightLeg);
            }
            case "sitting" -> {
                followerAS.setLeftArmPose(sittingLeftArm);
                followerAS.setRightArmPose(sittingRightArm);
                followerAS.setLeftLegPose(sittingLeftLeg);
                followerAS.setRightLegPose(sittingRightLeg);
                spawnSitParticles();
            }
            case "spinning" -> {
                followerAS.setLeftArmPose(spinningLeftArm);
                followerAS.setRightArmPose(spinningRightArm);
                followerAS.setLeftLegPose(spinningLeftLeg);
                followerAS.setRightLegPose(spinningRightLeg);
            }
        }
    }

    private void spawnSitParticles() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!poseName.equalsIgnoreCase("sitting") || !followerAS.isValid()) {
                    cancel();
                    return;
                }
                if (isPlayerInvisible) return;
                followerAS.getWorld().spawnParticle(Particle.CLOUD, followerAS.getLocation().add(0, -0.15, 0), 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(plugin, 0, 3);
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

                Followers.dataManager.setActiveArmorStand(nameTagAS.getUniqueId());
            }

            String test3 = Followers.dataManager.getFollowerUser(owner.getUniqueId()).getDisplayName();
            String test2 = Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", test3);
            String test1 = ChatColorHandler.translateAlternateColorCodes(test2);

            nameTagAS.setCustomName(test1);
            nameTagAS.setCustomNameVisible(true);
        } else {
            Followers.dataManager.setActiveArmorStand(nameTagAS.getUniqueId(), false);
            if (nameTagAS != null) nameTagAS.remove();
            nameTagAS = null;
        }
    }

    public void teleportToPlayer(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location playerLoc = player.getLocation();
            if (!followerAS.getLocation().getChunk().isLoaded()) followerAS.getLocation().getChunk().load();
            teleportArmorStands(playerLoc.add(1.5, 0, 1.5));
        }, 20);
    }

    private void teleportArmorStands(Location location) {
        followerAS.getLocation().getChunk().load();
        followerAS.teleport(location);
        if (nameTagAS != null) nameTagAS.teleport(location.add(0, 1, 0));
    }

    private double getArmorStandYOffset(ArmorStand armorStand) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Followers.getCurrentTick() + armorStand.getEntityId()));
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
