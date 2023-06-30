package me.dave.followers.entity;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.entity.poses.FollowerPose;
import me.dave.followers.entity.tasks.MoveTask;
import me.dave.followers.entity.tasks.ParticleTask;
import me.dave.followers.entity.tasks.ValidateTask;
import me.dave.followers.entity.tasks.VisibilityTask;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import me.dave.followers.data.FollowerHandler;
import me.dave.followers.data.FollowerUser;
import org.bukkit.util.Vector;

import java.util.UUID;

public class FollowerEntity {
    private static final NamespacedKey followerKey = new NamespacedKey(Followers.getInstance(), "Follower");
    private final Player player;
    private final ArmorStand bodyArmorStand;
    private ArmorStand nameArmorStand;
    private UUID nameArmorStandUUID;
    private String followerType;
    private boolean isPlayerInvisible;
    private FollowerPose pose;
    public boolean isAlive;
    private boolean dying;
    private MoveTask moveTask;
    private ParticleTask particleTask;
    private VisibilityTask visibilityTask;

    public FollowerEntity(Player player, String follower) {
        this.player = player;
        this.followerType = follower;
        this.isPlayerInvisible = player.isInvisible();
        this.isAlive = true;

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(this.player);
        followerUser.setFollowerEnabled(true);

        this.bodyArmorStand = summonBodyArmorStand();
        if (this.bodyArmorStand == null) {
            kill();
            return;
        }
        displayName(followerUser.isDisplayNameEnabled());

        setFollowerType(follower);
        setVisible(!player.isInvisible());

        new ValidateTask(this).runTaskTimer(Followers.getInstance(), 0, 1);
        startVisiblityTask();
        startMovement();

        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::reloadInventory, 5);
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return bodyArmorStand.getLocation().clone();
    }

    public FollowerHandler getType() {
        return Followers.followerManager.getFollower(followerType);
    }

    public String getDisplayName() {
        return Followers.dataManager.getFollowerUser(player).getDisplayName();
    }

    public boolean isPlayerInvisible() {
        return isPlayerInvisible;
    }

    public void setPlayerInvisible(boolean invisible) {
        isPlayerInvisible = invisible;
    }

    public ArmorStand getBodyArmorStand() {
        return bodyArmorStand;
    }

    public ArmorStand getNameArmorStand() {
        return nameArmorStand;
    }

    public void setFollowerType(String newFollower) {
        this.followerType = newFollower;

        Followers.dataManager.getFollowerUser(player).setFollowerType(newFollower);
        if (!player.isInvisible()) reloadInventory();
    }

    public void setDisplayNameVisible(boolean visible) {
        Followers.dataManager.getFollowerUser(player).setDisplayNameEnabled(visible);
        if (!player.isInvisible()) displayName(visible);
    }

    public void setDisplayName(String newName) {
        Followers.dataManager.getFollowerUser(player).setDisplayName(newName);
        setDisplayNameVisible(true);
        if (Followers.configManager.areHitboxesEnabled()) bodyArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
        else nameArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
    }

    public void setVisible(boolean visible) {
        FollowerHandler followerConfig = Followers.followerManager.getFollower(followerType);
        if (followerConfig == null) return;

        bodyArmorStand.setVisible(followerConfig.isVisible() && visible);
        displayName(visible && Followers.dataManager.getFollowerUser(player).isDisplayNameEnabled());

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
            FollowerHandler followerHandler = Followers.followerManager.getFollower(this.followerType);
            if (followerHandler == null) {
                FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
                if (followerUser != null) followerUser.disableFollowerEntity();
                else kill();
                return;
            }

            if (player.isInvisible()) return;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                setFollowerArmorSlot(equipmentSlot, followerHandler);
            }

            bodyArmorStand.setVisible(followerHandler.isVisible());
        }, 1);
    }

    public void setFollowerArmorSlot(EquipmentSlot equipmentSlot, FollowerHandler followerType) {
        EntityEquipment armorEquipment = bodyArmorStand.getEquipment();
        if (armorEquipment == null) return;
        ItemStack item = switch (equipmentSlot) {
            case HEAD -> followerType.getHead();
            case CHEST -> followerType.getChest();
            case LEGS -> followerType.getLegs();
            case FEET -> followerType.getFeet();
            case HAND -> followerType.getMainHand();
            case OFF_HAND -> followerType.getOffHand();
        };
        armorEquipment.setItem(equipmentSlot, item);
    }

    private ArmorStand summonBodyArmorStand() {
        Location spawnLoc = player.getLocation().add(1.5, 0, 0);
        Vector direction = player.getLocation().getDirection().setY(0);
        spawnLoc = spawnLoc.add(direction.rotateAroundY(0.5));
        float offSet = direction.setY(0).angle(spawnLoc.toVector());

        ArmorStand armorStand;
        if (!spawnLoc.getChunk().isLoaded()) return null;
        try {
            armorStand = player.getLocation().getWorld().spawn(spawnLoc, ArmorStand.class, (as -> {
                try {
                    as.setBasePlate(false);
                    as.setArms(true);
                    as.setInvulnerable(true);
                    as.setCanPickupItems(false);
                    as.setSmall(true);
                    as.setAI(false);
                    as.setGravity(false);
                    as.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, player.getUniqueId().toString());
                    if (!Followers.configManager.areHitboxesEnabled()) as.setMarker(true);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Followers.dataManager.addActiveArmorStand(armorStand.getUniqueId());
        return armorStand;
    }

    private ArmorStand summonNameArmorStand() {
        ArmorStand armorStand;
        if (!bodyArmorStand.getLocation().getChunk().isLoaded()) return null;
        try {
            armorStand = player.getLocation().getWorld().spawn(bodyArmorStand.getLocation().add(0, 1, 0), ArmorStand.class, (as -> {
                try {
                    as.setInvulnerable(true);
                    as.setVisible(false);
                    as.setMarker(true);
                    as.setAI(false);
                    as.setGravity(false);
                    as.getPersistentDataContainer().set(followerKey, PersistentDataType.STRING, "");
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }));
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        Followers.dataManager.addActiveArmorStand(armorStand.getUniqueId());
        return armorStand;
    }

    private void displayName(boolean display) {
        if (display) {
            if (nameArmorStand == null) {
                nameArmorStand = summonNameArmorStand();
                if (nameArmorStand == null) return;
                nameArmorStandUUID = nameArmorStand.getUniqueId();

                Followers.dataManager.addActiveArmorStand(nameArmorStand.getUniqueId());
            }

            String nickname = Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", Followers.dataManager.getFollowerUser(player).getDisplayName());
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

    public void kill() {
        stopMovement();
        stopParticles();

        if (bodyArmorStand != null) {
            bodyArmorStand.remove();
            Followers.dataManager.removeActiveArmorStand(bodyArmorStand.getUniqueId());
        }

        if (nameArmorStand != null) nameArmorStand.remove();
        Followers.dataManager.removeActiveArmorStand(nameArmorStandUUID);

        isAlive = false;
    }

    public void deactivate() {
        dying = true;
        kill();
    }

    public boolean isDying() {
        return dying;
    }

    //////////////////////////////
    //     Visibility Task      //
    //////////////////////////////

    private void startVisiblityTask() {
        stopVisiblityTask();
        visibilityTask = new VisibilityTask(this);
        visibilityTask.runTaskTimer(Followers.getInstance(), 0L, 20L);
    }

    private void stopVisiblityTask() {
        if (visibilityTask != null && !visibilityTask.isCancelled()) {
            visibilityTask.cancel();
            visibilityTask = null;
        }
    }

    //////////////////////////////
    //    Movement Functions    //
    //////////////////////////////

    private void startMovement() {
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

    //////////////////////////
    //    Pose Functions    //
    //////////////////////////

    public FollowerPose getPose() {
        return pose;
    }

    public void setPose(FollowerPose pose) {
        if (this.pose == pose) return;
        this.pose = pose;
        pose.pose(bodyArmorStand);
    }

    //////////////////////////////
    //    Particle Functions    //
    //////////////////////////////

    public void startParticles(Particle particle) {
        stopParticles();
        particleTask = new ParticleTask(this, particle);
        particleTask.runTaskTimer(Followers.getInstance(), 0, 3);
    }

    public void stopParticles() {
        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
            particleTask = null;
        }
    }
}
