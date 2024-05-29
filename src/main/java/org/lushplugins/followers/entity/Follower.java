package org.lushplugins.followers.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import me.tofaa.entitylib.wrapper.WrapperEntityEquipment;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.FollowerEntityChangeTypeEvent;
import org.lushplugins.followers.api.events.FollowerEntitySpawnEvent;
import org.lushplugins.followers.api.events.FollowerTickEvent;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.data.FollowerHandler;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.entity.tasks.*;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;

import java.util.concurrent.ConcurrentHashMap;

public class Follower {
    private final ConcurrentHashMap<String, FollowerTask> tasks = new ConcurrentHashMap<>();
    private WrapperLivingEntity bodyEntity;
    private WrapperEntity nametagEntity;
    private String followerType;
    private World world;
    private Vector3d target;
    private boolean alive = false;
    private boolean visible = true;
    private String displayName;
    private FollowerPose pose;

    public Follower(String followerType) {
        this.followerType = followerType;
    }

    public Follower(String followerType, boolean visible, String displayName) {
        this.followerType = followerType;
        this.visible = visible;
        this.displayName = displayName;
    }

    public WrapperLivingEntity getEntity() {
        return bodyEntity;
    }

    public boolean isEntityValid() {
        return bodyEntity != null;
    }

    public @Nullable Location getLocation() {
        return bodyEntity != null ? bodyEntity.getLocation().clone() : null;
    }

    public FollowerHandler getType() {
        return Followers.getInstance().getFollowerManager().getFollower(followerType);
    }

    public void setType(String followerType) {
        if (Followers.getInstance().callEvent(new FollowerEntityChangeTypeEvent(this, this.followerType, followerType))) {
            this.followerType = followerType;

            if (visible) {
                refreshEntity();
                reloadInventory();
            }
        }
    }

    public World getWorld() {
        return world;
    }

    public Vector3d getTarget() {
        return target;
    }

    public void setTarget(World world, Location location) {
        setTarget(world, location.getPosition());
    }

    public void setTarget(World world, Vector3d target) {
        this.world = world;
        this.target = target;
    }

    public boolean isAlive() {
        if (!isEntityValid()) {
            alive = false;
        }

        return alive;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        updateVisibility();
    }

    public void updateVisibility() {
        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerType);
        if (followerHandler == null) {
            return;
        }

        if (isEntityValid()) {
            bodyEntity.getEntityMeta().setInvisible(!followerHandler.isVisible() || !visible);
        }

        displayName(visible && displayName != null);

        if (visible) {
            reloadInventory();
        } else {
            clearInventory();
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;

        if (nametagEntity != null) {
            nametagEntity.getEntityMeta().setCustomName(ModernChatColorHandler.translate(Followers.getInstance().getConfigManager().getFollowerNicknameFormat()
                .replaceAll("%nickname%", displayName)));
        }
    }

    public void hideDisplayName() {
        this.displayName = null;
    }

    public FollowerPose getPose() {
        return pose;
    }

    public void setPose(FollowerPose pose) {
        if (this.pose == pose) {
            return;
        }

        this.pose = pose;

        if (isEntityValid() && bodyEntity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta) {
            pose.pose(armorStandMeta);
        }
    }

    public void setArmorSlot(EquipmentSlot equipmentSlot, FollowerHandler followerType) {
        if (isEntityValid()) {
            WrapperEntityEquipment equipment = bodyEntity.getEquipment();

            if (equipment != null) {
                ExtendedSimpleItemStack simpleItemStack;
                switch (equipmentSlot) {
                    case HELMET -> simpleItemStack = followerType.getHead();
                    case CHEST_PLATE -> simpleItemStack = followerType.getChest();
                    case LEGGINGS -> simpleItemStack = followerType.getLegs();
                    case BOOTS -> simpleItemStack = followerType.getFeet();
                    case MAIN_HAND -> simpleItemStack = followerType.getMainHand();
                    case OFF_HAND -> simpleItemStack = followerType.getOffHand();
                    default -> simpleItemStack = null; // Should never happen
                }

                if (simpleItemStack != null) {
                    equipment.setItem(equipmentSlot, SpigotConversionUtil.fromBukkitItemStack(simpleItemStack.asItemStack()));
                }
            }
        }
    }

    public void clearInventory() {
        if (isEntityValid()) {
            WrapperEntityEquipment equipment = bodyEntity.getEquipment();
            if (equipment == null) {
                return;
            }

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                equipment.setItem(equipmentSlot, new ItemStack.Builder().type(ItemTypes.AIR).build());
            }
        }
    }

    public void reloadInventory() {
        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(this.followerType);
        if (followerHandler == null) {
            kill();
            return;
        }

        if (!visible) {
            return;
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            setArmorSlot(equipmentSlot, followerHandler);
        }

        if (isEntityValid()) {
            bodyEntity.getEntityMeta().setInvisible(!followerHandler.isVisible());
        }
    }

    public boolean spawn() {
        if (Followers.getInstance().callEvent(new FollowerEntitySpawnEvent(this))) {
            refreshEntity();
            this.alive = true;
            updateVisibility();

            setType(followerType);


//            startTask(new ValidateTask(TaskId.VALIDATE, player));
//            startTask(new VisibilityTask(TaskId.VISIBILITY, player));
//            startTask(new MoveNearTask(TaskId.MOVE_NEAR));

            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::reloadInventory, 5);
            return true;
        } else {
            kill();
            return false;
        }
    }

    public void teleport(Location location) {
        bodyEntity.teleport(location);
    }

    public void kill() {
        alive = false;
        stopTasks(TaskId.MOVE_NEAR, TaskId.PARTICLE, TaskId.VALIDATE);

        if (bodyEntity != null) {
            bodyEntity.remove();
        }

        if (nametagEntity != null) {
            nametagEntity.remove();
        }
    }

    public void startParticles(ParticleType<?> particle) {
        if (isEntityValid()) {
            startTask(new ParticleTask(TaskId.PARTICLE + "_" + particle.getName().toString(), particle));
        }
    }


    ////////////////////////
    //    Task Handler    //
    ////////////////////////

    public void tick() {
        int currTick = Followers.getInstance().getCurrentTick();

        if (Followers.getInstance().callEvent(new FollowerTickEvent(this))) {
            tasks.values().forEach(task -> {
                if (currTick % task.getPeriod() == 0) {
                    try {
                        task.tick(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Nullable
    public FollowerTask getTask(String id) {
        return tasks.get(id);
    }

    public void startTask(FollowerTask task) {
        stopTask(task.getId());

        tasks.put(task.getId(), task);
    }

    public void stopTask(String taskType) {
        if (taskType.equals("all")) {
            tasks.forEach((aTaskType, task) -> {
                task.cancel(this);
                tasks.remove(taskType);
            });
            return;
        }

        FollowerTask task = tasks.get(taskType);

        if (task != null) {
            if (!task.isCancelled()) {
                task.cancel(this);
            }

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
            if (nametagEntity == null) {
                nametagEntity = summonNametagEntity();
                if (nametagEntity == null) {
                    return;
                }
            }

            String nickname = Followers.getInstance().getConfigManager().getFollowerNicknameFormat()
                .replaceAll("%nickname%", displayName);

            TextDisplayMeta textDisplayMeta = (TextDisplayMeta) nametagEntity.getEntityMeta();
            textDisplayMeta.setText(Component.text(nickname));
            // TODO: Fix ChatColorHandler
//            textDisplayMeta.setText(ModernChatColorHandler.translate(nickname));
        } else {
            if (nametagEntity != null) {
                nametagEntity.remove();
                nametagEntity = null;
            }
        }
    }

    private void refreshEntity() {
        if (this.getWorld() == null) {
            throw new IllegalStateException("Cannot spawn Follower without world defined");
        }

        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerType);

        WrapperLivingEntity entity;
        if (bodyEntity != null) {
            if (!bodyEntity.getEntityType().equals(followerHandler.getEntityType())) {
                // Handles changing the entity type
                entity = followerHandler.createEntity(bodyEntity.getEntityId(), bodyEntity.getUuid());
                entity.spawn(bodyEntity.getLocation());
            } else {
                entity = bodyEntity;
            }
        } else {
            entity = followerHandler.createEntity();
            entity.spawn(new Location(getTarget(), 0, 0));
//            entity.spawn(SpigotConversionUtil.fromBukkitLocation(player.getLocation().add(1.5, 0, 1.5)));
        }

        followerHandler.applyAttributes(entity);
        this.bodyEntity = entity;
        reloadInventory();

        // TODO: Implement proper tracking system (not in this class)
        this.getWorld().getPlayers().forEach(viewer -> entity.addViewer(viewer.getUniqueId()));
        // TODO: Remove on EntityLib implementation
        entity.refresh();
        refreshNametag();
    }

    public void refreshNametag() {
        if (nametagEntity == null) {
            return;
        }

        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerType);
        TextDisplayMeta textDisplayMeta = (TextDisplayMeta) nametagEntity.getEntityMeta();

        float translation = followerHandler.getEntityType().equals(EntityTypes.ARMOR_STAND) && !Followers.getInstance().getConfigManager().areHitboxesEnabled() ? 0.6f : 0.1f;
        textDisplayMeta.setTranslation(new Vector3f(0, translation, 0));

        if (!bodyEntity.hasPassenger(nametagEntity)) {
            bodyEntity.addPassenger(nametagEntity.getEntityId());
        }
    }

    private WrapperEntity summonNametagEntity() {
        if (!isEntityValid()) {
            return null;
        }

        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerType);
        WrapperEntity textDisplay;
        try {
            textDisplay = EntityLib.getApi().createEntity(EntityTypes.TEXT_DISPLAY);
            textDisplay.spawn(new Location(bodyEntity.getLocation().getPosition(), 0, 0));
            bodyEntity.addPassenger(textDisplay);

            TextDisplayMeta textDisplayMeta = (TextDisplayMeta) textDisplay.getEntityMeta();
            textDisplayMeta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
            textDisplayMeta.setBackgroundColor(0);
            textDisplayMeta.setShadow(true);

            float translation = followerHandler.getEntityType().equals(EntityTypes.ARMOR_STAND) && !Followers.getInstance().getConfigManager().areHitboxesEnabled() ? 0.6f : 0.1f;
            textDisplayMeta.setTranslation(new Vector3f(0, translation, 0));

            float scale = (float) followerHandler.getScale() + 0.25f;
            textDisplayMeta.setScale(new Vector3f(scale, scale, scale));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // TODO: Implement proper tracking system (not in this class)
        this.getWorld().getPlayers().forEach(viewer -> textDisplay.addViewer(viewer.getUniqueId()));
        // TODO: Remove on EntityLib implementation
        bodyEntity.refresh();

        return textDisplay;
    }
}
