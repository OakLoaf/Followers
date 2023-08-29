package me.dave.followers.entity;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.api.events.FollowerEntityChangeTypeEvent;
import me.dave.followers.api.events.FollowerEntityTickEvent;
import me.dave.followers.entity.poses.FollowerPose;
import me.dave.followers.entity.tasks.*;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import me.dave.followers.data.FollowerHandler;
import me.dave.followers.data.FollowerUser;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;

public class FollowerEntity {
    private final HashMap<String, AbstractTask> tasks = new HashMap<>();
    // TODO: Replace with single runnable ticking all followers
    private final BukkitRunnable ticker = new BukkitRunnable() {
        @Override
        public void run() {
            tick();
        }
    };
    private final Player player;
    private final ArmorStand bodyArmorStand;
    private ArmorStand nameArmorStand;
    private int ticksAlive;
    private UUID nameArmorStandUUID;
    private String followerType;
    private boolean alive;
    private boolean visible;
    private FollowerPose pose;

    public FollowerEntity(Player player, String follower) {
        this.player = player;
        this.followerType = follower;
        this.visible = !player.isInvisible();
        this.alive = true;
        this.ticksAlive = 0;

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

        startTask(FollowerTasks.getTask("validate", this));
        startVisiblityTask();
        startMovement();

        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::reloadInventory, 5);

        ticker.runTaskTimer(Followers.getInstance(), 2, 1);
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
        if (!Followers.getInstance().callEvent(new FollowerEntityChangeTypeEvent(this, this.followerType, followerType))) {
            return;
        }

        this.followerType = followerType;

        Followers.dataManager.getFollowerUser(player).setFollowerType(followerType);
        if (!player.isInvisible()) {
            reloadInventory();
        }
    }

    public String getDisplayName() {
        return Followers.dataManager.getFollowerUser(player).getDisplayName();
    }

    public void setDisplayName(String newName) {
        Followers.dataManager.getFollowerUser(player).setDisplayName(newName);
        setDisplayNameVisible(true);
        if (Followers.configManager.areHitboxesEnabled()) {
            bodyArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
        } else {
            nameArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
        }
    }

    public boolean isDisplayNameVisible() {
        return Followers.dataManager.getFollowerUser(player).isDisplayNameEnabled();
    }

    public void setDisplayNameVisible(boolean visible) {
        Followers.dataManager.getFollowerUser(player).setDisplayNameEnabled(visible);
        if (!player.isInvisible()) {
            displayName(visible);
        }
    }

    public FollowerPose getPose() {
        return pose;
    }

    public void setPose(FollowerPose pose) {
        if (this.pose == pose) {
            return;
        }

        this.pose = pose;
        pose.pose(bodyArmorStand);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        FollowerHandler followerConfig = Followers.followerManager.getFollower(followerType);
        if (followerConfig == null) {
            return;
        }

        bodyArmorStand.setVisible(followerConfig.isVisible() && visible);
        displayName(visible && Followers.dataManager.getFollowerUser(player).isDisplayNameEnabled());

        if (visible) {
            reloadInventory();
        } else {
            clearInventory();
        }
    }

    public void setArmorSlot(EquipmentSlot equipmentSlot, FollowerHandler followerType) {
        EntityEquipment armorEquipment = bodyArmorStand.getEquipment();
        if (armorEquipment == null) {
            return;
        }

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
        if (equipment == null) {
            return;
        }

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

            if (player.isInvisible()) {
                return;
            }

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                setArmorSlot(equipmentSlot, followerHandler);
            }

            bodyArmorStand.setVisible(followerHandler.isVisible());
        }, 1);
    }

    public boolean teleport(Location location) {
        if (bodyArmorStand.getLocation().getChunk().isLoaded()) {
            if (nameArmorStand != null) {
                nameArmorStand.teleport(location.clone().add(0, 1, 0));
            }

            return bodyArmorStand.teleport(location);
        } else {
            return false;
        }
    }

    public void kill() {
        alive = false;

        ticker.cancel();
        stopTasks("all");

        if (bodyArmorStand != null) {
            bodyArmorStand.remove();
            Followers.dataManager.removeActiveArmorStand(bodyArmorStand.getUniqueId());
        }

        if (nameArmorStand != null) {
            nameArmorStand.remove();
        }

        Followers.dataManager.removeActiveArmorStand(nameArmorStandUUID);
    }

    private void startMovement() {
        String strUUID = bodyArmorStand.getPersistentDataContainer().get(Followers.getInstance().getFollowerKey(), PersistentDataType.STRING);
        if (strUUID == null) {
            return;
        }

        Player player = Bukkit.getPlayer(UUID.fromString(strUUID));
        if (player == null) {
            return;
        }

        startTask(FollowerTasks.getTask("movement", this));
    }

    public void startParticles(Particle particle) {
        try {
            startTask(FollowerTasks.getClass("particle").getConstructor(FollowerEntity.class, Particle.class).newInstance(this, particle));
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void startVisiblityTask() {
        startTask(FollowerTasks.getTask("visibility", this));
    }


    ////////////////////////
    //    Task Handler    //
    ////////////////////////

    public int getTicksAlive() {
        return ticksAlive;
    }

    public void tick() {
        ticksAlive++;

        if (!Followers.getInstance().callEvent(new FollowerEntityTickEvent(this))) {
            return;
        }

        tasks.values().forEach(task -> {
            if (ticksAlive >= task.getStartTick() && ticksAlive % task.getPeriod() == 0) {
                task.tick();
            }
        });
    }

    public void startTask(AbstractTask task) {
        stopTask(task.getIdentifier());

        tasks.put(task.getIdentifier(), task);
    }

    public void stopTask(String taskType) {
        if (taskType.equals("all")) {
            tasks.forEach((aTaskType, task) -> {
                task.cancel();
                tasks.remove(taskType);
            });
            return;
        }

        AbstractTask task = tasks.get(taskType);

        if (task != null && !task.isCancelled()) {
            task.cancel();
            tasks.remove(taskType);
        }
    }

    public void stopTasks(String... taskTypes) {
        for (String taskType : taskTypes) {
            stopTask(taskType);
        }
    }

    private void displayName(boolean display) {
        if (display) {
            if (nameArmorStand == null) {
                nameArmorStand = summonNameArmorStand();
                if (nameArmorStand == null) {
                    return;
                }

                nameArmorStandUUID = nameArmorStand.getUniqueId();

                Followers.dataManager.addActiveArmorStand(nameArmorStand.getUniqueId());
            }

            String nickname = Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", Followers.dataManager.getFollowerUser(player).getDisplayName());
            nameArmorStand.setCustomName(ChatColorHandler.translateAlternateColorCodes(nickname));
            nameArmorStand.setCustomNameVisible(true);
        }
        else {
            if (nameArmorStand != null) {
                nameArmorStand.remove();
            }

            Followers.dataManager.removeActiveArmorStand(nameArmorStandUUID);
            nameArmorStand = null;
            nameArmorStandUUID = null;
        }
    }

    private ArmorStand summonBodyArmorStand() {
        Location location = player.getLocation().add(1.5, 0, 1.5);

        ArmorStand armorStand;
        if (!location.getChunk().isLoaded()) {
            return null;
        }

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
                    as.setMetadata("keep", new FixedMetadataValue(Followers.getInstance(), null));
                    as.getPersistentDataContainer().set(Followers.getInstance().getFollowerKey(), PersistentDataType.STRING, player.getUniqueId().toString());
                    if (!Followers.configManager.areHitboxesEnabled()) {
                        as.setMarker(true);
                    }

                    Followers.dataManager.addActiveArmorStand(as.getUniqueId());
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return armorStand;
    }

    private ArmorStand summonNameArmorStand() {
        Location location = bodyArmorStand.getLocation();

        ArmorStand armorStand;
        if (!location.getChunk().isLoaded()) {
            return null;
        }

        try {
            armorStand = location.getWorld().spawn(bodyArmorStand.getLocation().add(0, 1, 0), ArmorStand.class, (as -> {
                try {
                    as.setInvulnerable(true);
                    as.setVisible(false);
                    as.setMarker(true);
                    as.setAI(false);
                    as.setGravity(false);
                    as.setMetadata("keep", new FixedMetadataValue(Followers.getInstance(), "keep"));
                    as.getPersistentDataContainer().set(Followers.getInstance().getFollowerKey(), PersistentDataType.STRING, "");

                    Followers.dataManager.addActiveArmorStand(as.getUniqueId());
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }));
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        return armorStand;
    }
}
