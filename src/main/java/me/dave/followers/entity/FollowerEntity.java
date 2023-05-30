package me.dave.followers.entity;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import me.dave.followers.datamanager.FollowerHandler;
import me.dave.followers.datamanager.FollowerUser;

import java.util.UUID;

public class FollowerEntity {
    private final NamespacedKey followerKey = new NamespacedKey(Followers.getInstance(), "Follower");
    protected final Player owner;
    protected final ArmorStand bodyArmorStand;
    protected ArmorStand nameArmorStand;
    private UUID nameArmorStandUUID;
    private String follower;
    protected boolean isPlayerInvisible;
    private String pose;
    private boolean isEnabled;
    private MoveTask moveTask;
    private ParticleTask particleTask;

    // Default Pose
    private final EulerAngle defaultLeftArm = new EulerAngle(-0.17453292519943295, 0, -0.17453292519943295);
    private final EulerAngle defaultRightArm = new EulerAngle(-0.2617993877991494, 0, 0.17453292519943295);
    private final EulerAngle defaultLeftLeg = new EulerAngle(-0.017453292519943295, 0, -0.017453292519943295);
    private final EulerAngle defaultRightLeg = new EulerAngle(0.017453292519943295, 0, 0.017453292519943295);
    // Sitting Pose
    private final EulerAngle sittingLeftArm = new EulerAngle(-0.78, 0, -0.17453292519943295);
    private final EulerAngle sittingRightArm = new EulerAngle(-0.78, 0, 0.17453292519943295);
    private final EulerAngle sittingLeftLeg = new EulerAngle(4.6, -0.222, 0);
    private final EulerAngle sittingRightLeg = new EulerAngle(4.6, 0.222, 0);
    // Spinning Pose
    private final EulerAngle spinningLeftArm = new EulerAngle(-0.17453292519943295, 0, -0.17453292519943295);
    private final EulerAngle spinningRightArm = new EulerAngle(-0.2617993877991494, 0, 0.17453292519943295);
    private final EulerAngle spinningLeftLeg = new EulerAngle(-0.017453292519943295, 0, -0.017453292519943295);
    private final EulerAngle spinningRightLeg = new EulerAngle(0.017453292519943295, 0, 0.017453292519943295);


    public FollowerEntity(Player player, String follower) {
        this.owner = player;
        this.follower = follower;
        this.isPlayerInvisible = player.isInvisible();

        this.bodyArmorStand = summonBodyArmorStand();
        setFollower(follower);
        setVisible(!player.isInvisible());

//        spawn();
    }

    public void setFollower(String newFollower) {
        this.follower = newFollower;

        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setFollower(newFollower);
        if (!owner.isInvisible()) reloadInventory();
    }

    public void setDisplayNameVisible(boolean visible) {
        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setDisplayNameEnabled(visible);
        if (!owner.isInvisible()) {
            if (Followers.configManager.areHitboxesEnabled()) bodyArmorStand.setCustomNameVisible(visible);
            else displayName(visible);
        }
    }

    public void setDisplayName(String newName) {
        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setDisplayName(newName);
        setDisplayNameVisible(true);
        if (Followers.configManager.areHitboxesEnabled()) bodyArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
        else nameArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
    }

    public void setVisible(boolean visible) {
        FollowerHandler followerConfig = Followers.followerManager.getFollower(follower);
        if (followerConfig == null) return;
        bodyArmorStand.setVisible(followerConfig.isVisible() && visible);
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(owner.getUniqueId());
        if (followerUser == null) return;
        if (!Followers.configManager.areHitboxesEnabled() && followerUser.isDisplayNameEnabled()) displayName(visible);
        if (visible) reloadInventory();
        else clearInventory();
    }

    public void clearInventory() {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            bodyArmorStand.getEquipment().setItem(equipmentSlot, new ItemStack(Material.AIR));
        }
    }

    public void reloadInventory() {
        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> {
            if (owner.isInvisible()) return;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                setFollowerArmorSlot(equipmentSlot, follower);
            }
            FollowerHandler followerEntity = Followers.followerManager.getFollower(follower);
            if (followerEntity == null) return;
            bodyArmorStand.setVisible(followerEntity.isVisible());
        }, 1);
    }

    public void setFollowerArmorSlot(EquipmentSlot equipmentSlot, String followerName) {
        if (!Followers.followerManager.getFollowers().containsKey(followerName)) return;
        EntityEquipment armorEquipment = bodyArmorStand.getEquipment();
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

    public ArmorStand summonBodyArmorStand() {
        ArmorStand armorStand = owner.getLocation().getWorld().spawn(owner.getLocation().add(-1.5, 0, 1.5), ArmorStand.class, (as -> {
            try {
                as.setBasePlate(false);
                as.setArms(true);
                as.setInvulnerable(true);
                as.setCanPickupItems(false);
                as.setSmall(true);
                as.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
                if (!Followers.configManager.areHitboxesEnabled()) as.setMarker(true);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }));

        Followers.dataManager.addActiveArmorStand(armorStand.getUniqueId());

        return armorStand;
    }

    private ArmorStand summonNameArmorStand() {
        ArmorStand armorStand = owner.getLocation().getWorld().spawn(bodyArmorStand.getLocation().add(0, 1, 0), ArmorStand.class, (as -> {
            try {
                as.setInvulnerable(true);
                as.setVisible(false);
                as.setMarker(true);
                as.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, "");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }));

        Followers.dataManager.addActiveArmorStand(armorStand.getUniqueId());

        return armorStand;
    }

    public void spawn() {
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(owner.getUniqueId());
        Followers.dataManager.putInPlayerFollowerMap(owner.getUniqueId(), this);
        if (followerUser != null) followerUser.setFollowerEnabled(true);
        isEnabled = true;

//        bodyArmorStand = owner.getLocation().getWorld().spawn(owner.getLocation().add(-1.5, 0, 1.5), ArmorStand.class, (armorStand -> {
//            try {
//                armorStand.setBasePlate(false);
//                armorStand.setArms(true);
//                armorStand.setInvulnerable(true);
//                armorStand.setCanPickupItems(false);
//                armorStand.setSmall(true);
//                armorStand.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
//        }));

//        Followers.dataManager.setActiveArmorStand(bodyArmorStand.getUniqueId());

//        setVisible(!owner.isInvisible());

        if (followerUser.isDisplayNameEnabled()) {
            if (Followers.configManager.areHitboxesEnabled()) {
                bodyArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", followerUser.getDisplayName())));
                bodyArmorStand.setCustomNameVisible(followerUser.isDisplayNameEnabled());
            } else if (nameArmorStand != null) {
                nameArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", followerUser.getDisplayName())));
                nameArmorStand.setCustomNameVisible(followerUser.isDisplayNameEnabled());
            }
        }

//        setFollower(follower);
        restartMovement();
    }

    public void kill() {
        kill(isEnabled);
    }

    public void kill(boolean respawn) {
        if (bodyArmorStand == null) return;

        Followers.dataManager.removeActiveArmorStand(bodyArmorStand.getUniqueId());

        Followers.dataManager.removeFromPlayerFollowerMap(owner.getUniqueId());
        bodyArmorStand.remove();
        if (nameArmorStand != null) nameArmorStand.remove();

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(owner.getUniqueId());
        if (followerUser != null && owner.isOnline()) followerUser.setFollowerEnabled(false);
        isEnabled = false;

        if (respawn) spawn();
    }


    //////////////////////////////
    //    Movement Functions    //
    //////////////////////////////

    private void restartMovement() {
        String strUUID = bodyArmorStand.getPersistentDataContainer().get(followerKey, PersistentDataType.STRING);
        if (strUUID == null) return;
        Player player = Bukkit.getPlayer(UUID.fromString(strUUID));
        if (player == null) return;

        stopMovement();
        moveTask = new MoveTask(this);
        moveTask.runTaskTimer(Followers.getInstance(), 0L, 1L);
    }

    private void stopMovement() {
        if (moveTask != null && !moveTask.isCancelled()) {
            moveTask.cancel();
            moveTask = null;
        }
    }

    public void setPose(String poseName) {
        if (poseName.equalsIgnoreCase(this.pose)) return;
        this.pose = poseName;

        switch (poseName) {
            case "default" -> {
                bodyArmorStand.setLeftArmPose(defaultLeftArm);
                bodyArmorStand.setRightArmPose(defaultRightArm);
                bodyArmorStand.setLeftLegPose(defaultLeftLeg);
                bodyArmorStand.setRightLegPose(defaultRightLeg);
            }
            case "sitting" -> {
                bodyArmorStand.setLeftArmPose(sittingLeftArm);
                bodyArmorStand.setRightArmPose(sittingRightArm);
                bodyArmorStand.setLeftLegPose(sittingLeftLeg);
                bodyArmorStand.setRightLegPose(sittingRightLeg);
                spawnSitParticles();
            }
            case "spinning" -> {
                bodyArmorStand.setLeftArmPose(spinningLeftArm);
                bodyArmorStand.setRightArmPose(spinningRightArm);
                bodyArmorStand.setLeftLegPose(spinningLeftLeg);
                bodyArmorStand.setRightLegPose(spinningRightLeg);
            }
        }
    }

    public String getPose() {
        return pose;
    }

    private void spawnSitParticles() {
        particleTask = new ParticleTask(this, Particle.CLOUD);
        particleTask.runTaskTimer(Followers.getInstance(), 0, 3);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!pose.equalsIgnoreCase("sitting") || !bodyArmorStand.isValid()) {
                    cancel();
                    return;
                }
                if (isPlayerInvisible) return;
                bodyArmorStand.getWorld().spawnParticle(Particle.CLOUD, bodyArmorStand.getLocation().add(0, -0.15, 0), 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(Followers.getInstance(), 0, 3);
    }

    private void stopParticles() {
        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void displayName(boolean display) {
        if (display) {
            if (nameArmorStand == null) {
                nameArmorStand = summonNameArmorStand();
                nameArmorStandUUID = nameArmorStand.getUniqueId();

                Followers.dataManager.addActiveArmorStand(nameArmorStand.getUniqueId());
            }

            String nickname = Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", Followers.dataManager.getFollowerUser(owner.getUniqueId()).getDisplayName());
            nameArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(nickname));
            nameArmorStand.setCustomNameVisible(true);
        }
        else {
            if (nameArmorStand != null) nameArmorStand.remove();
            Followers.dataManager.removeActiveArmorStand(nameArmorStandUUID);
            nameArmorStand = null;
            nameArmorStandUUID = null;
        }
    }
}
