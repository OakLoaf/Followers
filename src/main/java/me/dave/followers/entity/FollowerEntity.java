package me.dave.followers.entity;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.api.events.FollowerEntityChangeTypeEvent;
import me.dave.followers.api.events.FollowerEntitySpawnEvent;
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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;

public class FollowerEntity {
    private final HashMap<String, AbstractTask> tasks = new HashMap<>();
    private final Player player;
    private ArmorStand bodyEntity;
    private ArmorStand nametagEntity;
    private int ticksAlive;
    private UUID nameArmorStandUUID;
    private String followerType;
    private boolean alive;
    private boolean visible;
    private FollowerPose pose;

    public FollowerEntity(Player player, String followerType) {
        this.player = player;
        this.followerType = followerType;
        this.visible = !player.isInvisible();
        this.alive = false;
        this.ticksAlive = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public ArmorStand getBodyEntity() {
        return bodyEntity;
    }

    public boolean isBodyEntityValid() {
        return bodyEntity != null && bodyEntity.isValid();
    }

    public ArmorStand getNametagEntity() {
        return nametagEntity;
    }

    public boolean isNametagEntityValid() {
        return nametagEntity != null && nametagEntity.isValid();
    }

    @Nullable
    public Location getLocation() {
        return bodyEntity != null ? bodyEntity.getLocation() : null;
    }

    public boolean isAlive() {
        if (!isBodyEntityValid()) {
            alive = false;
        }

        return alive;
    }

    public FollowerHandler getType() {
        return Followers.followerManager.getFollower(followerType);
    }

    public void setType(String followerType) {
        if (Followers.getInstance().callEvent(new FollowerEntityChangeTypeEvent(this, this.followerType, followerType))) {
            this.followerType = followerType;

            Followers.dataManager.getFollowerUser(player).setFollowerType(followerType);
            if (!player.isInvisible()) {
                reloadInventory();
            }
        }
    }

    public String getDisplayName() {
        return Followers.dataManager.getFollowerUser(player).getDisplayName();
    }

    public void setDisplayName(String newName) {
        Followers.dataManager.getFollowerUser(player).setDisplayName(newName);
        showDisplayName(true);

        if (Followers.configManager.areHitboxesEnabled()) {
            if (isBodyEntityValid()) {
                bodyEntity.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
            }
        } else {
            if (isNametagEntityValid()) {
                nametagEntity.setCustomName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
            }
        }
    }

    public boolean isDisplayNameVisible() {
        return Followers.dataManager.getFollowerUser(player).isDisplayNameEnabled();
    }

    public void showDisplayName(boolean visible) {
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

        if (isBodyEntityValid()) {
            pose.pose(bodyEntity);
        }
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

        if (isBodyEntityValid()) {
            bodyEntity.setVisible(followerConfig.isVisible() && visible);
        }

        displayName(visible && Followers.dataManager.getFollowerUser(player).isDisplayNameEnabled());

        if (visible) {
            reloadInventory();
        } else {
            clearInventory();
        }
    }

    public void setArmorSlot(EquipmentSlot equipmentSlot, FollowerHandler followerType) {
        if (isBodyEntityValid()) {
            EntityEquipment equipment = bodyEntity.getEquipment();

            if (equipment != null) {
                ItemStack item = switch (equipmentSlot) {
                    case HEAD -> followerType.getHead();
                    case CHEST -> followerType.getChest();
                    case LEGS -> followerType.getLegs();
                    case FEET -> followerType.getFeet();
                    case HAND -> followerType.getMainHand();
                    case OFF_HAND -> followerType.getOffHand();
                };

                equipment.setItem(equipmentSlot, item);
            }
        }
    }

    public void clearInventory() {
        if (isBodyEntityValid()) {
            EntityEquipment equipment = bodyEntity.getEquipment();
            if (equipment == null) {
                return;
            }

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                equipment.setItem(equipmentSlot, new ItemStack(Material.AIR));
            }
        }
    }

    public void reloadInventory() {
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

        if (isBodyEntityValid()) {
            bodyEntity.setVisible(followerHandler.isVisible());
        }
    }

    public boolean spawn() {
        if (Followers.getInstance().callEvent(new FollowerEntitySpawnEvent(this))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);

            this.bodyEntity = summonBodyEntity();
            if (!isBodyEntityValid()) {
                kill();
                return false;
            }

            displayName(followerUser.isDisplayNameEnabled());

            followerUser.setFollowerEnabled(true);
            setType(followerType);
            setVisible(!player.isInvisible());

            startTask(FollowerTasks.getTask("validate", this));
            startTask(FollowerTasks.getTask("visibility", this));
            startMovement();

            this.alive = true;
            this.ticksAlive = 0;

            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::reloadInventory, 5);
            return true;
        } else {
            kill();
            return false;
        }
    }

    public boolean teleport(Location location) {
        if (isBodyEntityValid()) {
            return false;
        }

        if (bodyEntity.getLocation().getChunk().isLoaded()) {
            if (nametagEntity != null) {
                nametagEntity.teleport(location.clone().add(0, 1, 0));
            }

            return bodyEntity.teleport(location);
        } else {
            return false;
        }
    }

    public void kill() {
        alive = false;

        stopTasks("all");

        if (bodyEntity != null) {
            bodyEntity.remove();
            Followers.dataManager.removeActiveArmorStand(bodyEntity.getUniqueId());
        }

        if (nametagEntity != null) {
            nametagEntity.remove();
        }

        Followers.dataManager.removeActiveArmorStand(nameArmorStandUUID);
    }

    private void startMovement() {
        if (isBodyEntityValid()) {
            String strUUID = bodyEntity.getPersistentDataContainer().get(Followers.getInstance().getFollowerKey(), PersistentDataType.STRING);

            if (strUUID != null) {
                Player player = Bukkit.getPlayer(UUID.fromString(strUUID));
                if (player != null) {
                    startTask(FollowerTasks.getTask("movement", this));
                }
            }
        }
    }

    public void startParticles(Particle particle) {
        if (isBodyEntityValid()) {
            try {
                startTask(FollowerTasks.getClass("particle").getConstructor(FollowerEntity.class, Particle.class).newInstance(this, particle));
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }


    ////////////////////////
    //    Task Handler    //
    ////////////////////////

    public int getTicksAlive() {
        return ticksAlive;
    }

    public void tick() {
        ticksAlive++;

        if (Followers.getInstance().callEvent(new FollowerEntityTickEvent(this))) {
            tasks.values().forEach(task -> {
                if (ticksAlive >= task.getStartTick() && ticksAlive % task.getPeriod() == 0) {
                    task.tick();
                }
            });
        }
    }

    @Nullable
    public AbstractTask getTask(String id) {
        return tasks.get(id);
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
            if (!isNametagEntityValid()) {
                nametagEntity = summonNametagEntity();
                if (!isNametagEntityValid()) {
                    return;
                }

                nameArmorStandUUID = nametagEntity.getUniqueId();
            }

            String nickname = Followers.configManager.getFollowerNicknameFormat().replaceAll("%nickname%", Followers.dataManager.getFollowerUser(player).getDisplayName());
            nametagEntity.setCustomName(ChatColorHandler.translateAlternateColorCodes(nickname));
            nametagEntity.setCustomNameVisible(true);
        }
        else {
            if (isNametagEntityValid()) {
                nametagEntity.remove();
            }

            Followers.dataManager.removeActiveArmorStand(nameArmorStandUUID);
            nametagEntity = null;
            nameArmorStandUUID = null;
        }
    }

    private ArmorStand summonBodyEntity() {
        World world = player.getWorld();
        Location location = player.getLocation().add(1.5, 0, 1.5);

        ArmorStand armorStand;
        if (!location.getChunk().isLoaded()) {
            return null;
        }

        try {
            armorStand = world.spawn(location, ArmorStand.class, (as -> {
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

    private ArmorStand summonNametagEntity() {
        if (!isBodyEntityValid()) {
            return null;
        }

        World world = bodyEntity.getWorld();
        Location location = bodyEntity.getLocation();

        ArmorStand armorStand;
        if (!location.getChunk().isLoaded()) {
            return null;
        }

        try {
            armorStand = world.spawn(location.clone().add(0, 1, 0), ArmorStand.class, (as -> {
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
