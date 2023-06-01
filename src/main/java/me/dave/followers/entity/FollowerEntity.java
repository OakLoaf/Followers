package me.dave.followers.entity;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.entity.pose.FollowerPose;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import me.dave.followers.data.FollowerHandler;
import me.dave.followers.data.FollowerUser;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class FollowerEntity {
    private static final NamespacedKey followerKey = new NamespacedKey(Followers.getInstance(), "Follower");
    protected final Player owner;
    protected final ArmorStand bodyArmorStand;
    protected ArmorStand nameArmorStand;
    private UUID nameArmorStandUUID;
    private String followerType;
    protected boolean isPlayerInvisible;
    private FollowerPose pose;
    public boolean isAlive;
    private MoveTask moveTask;
    private ParticleTask particleTask;

    public FollowerEntity(Player player, String follower) {
        this.owner = player;
        this.followerType = follower;
        this.isPlayerInvisible = player.isInvisible();
        this.isAlive = true;

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(owner.getUniqueId());
        if (followerUser != null) followerUser.setFollowerEnabled(true);

        this.bodyArmorStand = summonBodyArmorStand();
        if (followerUser != null) displayName(followerUser.isDisplayNameEnabled());

        setFollowerType(follower);
        setVisible(!player.isInvisible());

        validateTask();
        startMovement();
    }

    public void setFollowerType(String newFollower) {
        this.followerType = newFollower;

        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setFollowerType(newFollower);
        if (!owner.isInvisible()) reloadInventory();
    }

    public void setDisplayNameVisible(boolean visible) {
        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setDisplayNameEnabled(visible);
        if (!owner.isInvisible()) displayName(visible);
    }

    public void setDisplayName(String newName) {
        Followers.dataManager.getFollowerUser(owner.getUniqueId()).setDisplayName(newName);
        setDisplayNameVisible(true);
        if (Followers.configManager.areHitboxesEnabled()) bodyArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
        else nameArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
    }

    public void setVisible(boolean visible) {
        FollowerHandler followerConfig = Followers.followerManager.getFollower(followerType);
        if (followerConfig == null) return;
        bodyArmorStand.setVisible(followerConfig.isVisible() && visible);
        displayName(visible);
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
                setFollowerArmorSlot(equipmentSlot, followerType);
            }
            FollowerHandler followerEntity = Followers.followerManager.getFollower(followerType);
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

    private ArmorStand summonBodyArmorStand() {
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

    public void validateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bodyArmorStand == null || !bodyArmorStand.isValid() || !isAlive) {
                    Followers.dataManager.getFollowerUser(owner.getUniqueId()).respawnFollowerEntity();
                    cancel();
                    return;
                }
                if (!owner.isOnline()) Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> kill(), 5);
            }
        }.runTaskTimer(Followers.getInstance(), 0, 1);
    }
}
