package me.dave.followers.entity;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.entity.poses.FollowerPose;
import me.dave.followers.entity.tasks.*;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import me.dave.followers.data.FollowerHandler;
import me.dave.followers.data.FollowerUser;

import java.util.HashMap;
import java.util.UUID;

public class FollowerEntity {
    private static final NamespacedKey followerKey = new NamespacedKey(Followers.getInstance(), "Follower");
    private final HashMap<FollowerTaskType, AbstractTask> taskMap = new HashMap<>();
    private final Player player;
    private final ArmorStand bodyArmorStand;
    private ArmorStand nameArmorStand;
    private UUID nameArmorStandUUID;
    private String followerType;
    private boolean alive;
    private boolean playerInvisible;
    private FollowerPose pose;

    public FollowerEntity(Player player, String follower) {
        this.player = player;
        this.followerType = follower;
        this.playerInvisible = player.isInvisible();
        this.alive = true;

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(this.player);
        followerUser.setFollowerEnabled(true);

        this.bodyArmorStand = summonBodyArmorStand();
        if (this.bodyArmorStand == null) {
            kill();
            return;
        }
        displayName(followerUser.isDisplayNameEnabled());

        setType(follower);
        setVisible(!player.isInvisible());

        startTask(new ValidateTask(this), 0, 1);
        startVisiblityTask();
        startMovement();

        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::reloadInventory, 5);
    }

    public Player getPlayer() {
        return player;
    }

    public ArmorStand getBodyArmorStand() {
        return bodyArmorStand;
    }

    public ArmorStand getNameArmorStand() {
        return nameArmorStand;
    }

    public Location getLocation() {
        return bodyArmorStand.getLocation().clone();
    }

    public boolean isAlive() {
        return alive;
    }

    public FollowerHandler getType() {
        return Followers.followerManager.getFollower(followerType);
    }

    public void setType(String followerType) {
        this.followerType = followerType;

        Followers.dataManager.getFollowerUser(player).setFollowerType(followerType);
        if (!player.isInvisible()) reloadInventory();
    }

    public String getDisplayName() {
        return Followers.dataManager.getFollowerUser(player).getDisplayName();
    }

    public void setDisplayName(String newName) {
        Followers.dataManager.getFollowerUser(player).setDisplayName(newName);
        setDisplayNameVisible(true);
        if (Followers.configManager.areHitboxesEnabled()) bodyArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
        else nameArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
    }

    public boolean isDisplayNameVisible() {
        return Followers.dataManager.getFollowerUser(player).isDisplayNameEnabled();
    }

    public void setDisplayNameVisible(boolean visible) {
        Followers.dataManager.getFollowerUser(player).setDisplayNameEnabled(visible);
        if (!player.isInvisible()) displayName(visible);
    }

    public FollowerPose getPose() {
        return pose;
    }

    public void setPose(FollowerPose pose) {
        if (this.pose == pose) return;
        this.pose = pose;
        pose.pose(bodyArmorStand);
    }

    public boolean isPlayerInvisible() {
        return playerInvisible;
    }

    public void setPlayerInvisible(boolean invisible) {
        playerInvisible = invisible;
    }

    public void setVisible(boolean visible) {
        FollowerHandler followerConfig = Followers.followerManager.getFollower(followerType);
        if (followerConfig == null) return;

        bodyArmorStand.setVisible(followerConfig.isVisible() && visible);
        displayName(visible && Followers.dataManager.getFollowerUser(player).isDisplayNameEnabled());

        if (visible) reloadInventory();
        else clearInventory();
    }

    public void setArmorSlot(EquipmentSlot equipmentSlot, FollowerHandler followerType) {
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

    public void clearInventory() {
        EntityEquipment equipment = bodyArmorStand.getEquipment();
        if (equipment == null) return;

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            equipment.setItem(equipmentSlot, new ItemStack(Material.AIR));
        }
    }

    public void reloadInventory() {
        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> {
            FollowerHandler followerHandler = Followers.followerManager.getFollower(this.followerType);
            if (followerHandler == null) {
                FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
                followerUser.disableFollowerEntity();
                return;
            }

            if (player.isInvisible()) return;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                setArmorSlot(equipmentSlot, followerHandler);
            }

            bodyArmorStand.setVisible(followerHandler.isVisible());
        }, 1);
    }

    public boolean teleport(Location location) {
        if (bodyArmorStand.getLocation().getChunk().isLoaded()) return (bodyArmorStand.teleport(location) && nameArmorStand.teleport(location));
        else return false;
    }

    public void kill() {
        alive = false;

        stopTasks(FollowerTaskType.MOVEMENT, FollowerTaskType.PARTICLE);

        if (bodyArmorStand != null) {
            bodyArmorStand.remove();
            Followers.dataManager.removeActiveArmorStand(bodyArmorStand.getUniqueId());
        }

        if (nameArmorStand != null) nameArmorStand.remove();
        Followers.dataManager.removeActiveArmorStand(nameArmorStandUUID);
    }

    private void startMovement() {
        String strUUID = bodyArmorStand.getPersistentDataContainer().get(followerKey, PersistentDataType.STRING);
        if (strUUID == null) return;
        Player player = Bukkit.getPlayer(UUID.fromString(strUUID));
        if (player == null) return;

        stopTask(FollowerTaskType.MOVEMENT);
        startTask(new MovementTask(this), 0, 1);
    }

    public void startParticles(Particle particle) {
        stopTask(FollowerTaskType.PARTICLE);
        startTask(new ParticleTask(this, particle), 0, 3);
    }

    private void startVisiblityTask() {
        stopTask(FollowerTaskType.VISIBILITY);
        startTask(new VisibilityTask(this), 0, 20);
    }


    ////////////////////////
    //    Task Handler    //
    ////////////////////////

    public void startTask(AbstractTask task, int delay, int period) {
        stopTask(task.getType());

        taskMap.put(task.getType(), task);
        task.runTaskTimer(Followers.getInstance(), delay, period);
    }

    public void stopTask(FollowerTaskType taskType) {
        AbstractTask task = taskMap.get(taskType);

        if (task != null && !task.isCancelled()) {
            task.cancel();
            taskMap.remove(taskType);
        }
    }

    public void stopTasks(FollowerTaskType... taskTypes) {
        for (FollowerTaskType taskType : taskTypes) {
            stopTask(taskType);
        }
    }

    public void stopTasks() {
        taskMap.forEach((taskType, task) -> {
            task.cancel();
            taskMap.remove(taskType);
        });
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

    private ArmorStand summonBodyArmorStand() {
        Location location = player.getLocation().add(1.5, 0, 1.5);

        ArmorStand armorStand;
        if (!location.getChunk().isLoaded()) return null;
        try {
            armorStand = location.getWorld().spawn(location, ArmorStand.class, (as -> {
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
        Location location = bodyArmorStand.getLocation();

        ArmorStand armorStand;
        if (!location.getChunk().isLoaded()) return null;
        try {
            armorStand = location.getWorld().spawn(bodyArmorStand.getLocation().add(0, 1, 0), ArmorStand.class, (as -> {
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
}
