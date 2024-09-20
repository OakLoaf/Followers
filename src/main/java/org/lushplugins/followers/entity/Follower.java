package org.lushplugins.followers.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.User;
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
import me.tofaa.entitylib.wrapper.WrapperPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.FollowerEntityChangeTypeEvent;
import org.lushplugins.followers.api.events.FollowerEntitySpawnEvent;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.config.FollowerHandler;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.entity.tasks.*;

import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.SkinData;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;

import java.util.HashSet;

public class Follower {
    private final HashSet<String> tasks = new HashSet<>();
    private String followerType;
    private WrapperLivingEntity entity;
    private WrapperEntity nameTagEntity;
    private World world;
    private Vector3d target;
    private String displayName;
    private FollowerPose pose;

    public Follower(String followerType) {
        this.followerType = followerType;
    }

    public Follower(String followerType, String displayName) {
        this.followerType = followerType;
        this.displayName = displayName;
    }

    public String getType() {
        return followerType;
    }

    public FollowerHandler getTypeHandler() {
        return Followers.getInstance().getFollowerManager().getFollower(followerType);
    }

    public void setType(String followerType) {
        if (this.followerType.equals(followerType)) {
            return;
        }

        if (Followers.getInstance().callEvent(new FollowerEntityChangeTypeEvent(this, this.followerType, followerType))) {
            this.followerType = followerType;
            refresh();
        }
    }

    public @Nullable WrapperLivingEntity getEntity() {
        return entity;
    }

    public @Nullable WrapperEntity getNameTagEntity() {
        return nameTagEntity;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        if (this.world != world) {
            this.world = world;

            if (entity != null) {
                entity.getViewers().forEach(entity::removeViewer);
            }

            if (nameTagEntity != null) {
                nameTagEntity.getViewers().forEach(nameTagEntity::removeViewer);
            }
        }
    }

    public @Nullable Location getLocation() {
        return entity != null ? entity.getLocation().clone() : null;
    }

    public void teleport(Location location) {
        entity.teleport(location);
    }

    public Vector3d getTarget() {
        return target;
    }

    public void setTarget(World world, Location location) {
        setTarget(world, location.getPosition());
    }

    public void setTarget(World world, Vector3d target) {
        this.setWorld(world);
        this.target = target;
    }

    public boolean isSpawned() {
        return entity != null && entity.isSpawned();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;

        if (displayName != null) {
            refreshNametag();
        } else {
            if (nameTagEntity != null) {
                nameTagEntity.remove();
                nameTagEntity = null;
            }
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

        if (entity != null) {
            Followers.getInstance().getFollowerManager().getPoseRegistry().applyPose(entity, pose);
        }
    }

    public void setArmorSlot(EquipmentSlot equipmentSlot, FollowerHandler followerType) {
        // TODO: Remove on EntityLib implementation
        if (equipmentSlot.name().equals("BODY")) {
            Followers.getInstance().getLogger().warning("Equipment slot 'body' is not currently supported");
            return;
        }

        if (entity != null) {
            WrapperEntityEquipment equipment = entity.getEquipment();

            if (equipment != null) {
                ExtendedSimpleItemStack simpleItemStack = followerType.getEquipmentSlot(equipmentSlot);
                if (simpleItemStack != null) {
                    equipment.setItem(
                        equipmentSlot,
                        SpigotConversionUtil.fromBukkitItemStack(simpleItemStack.asItemStack()));
                }
            }
        }
    }

    public void clearInventory() {
        if (entity != null) {
            WrapperEntityEquipment equipment = entity.getEquipment();
            if (equipment == null) {
                return;
            }

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                try {
                    equipment.setItem(
                        com.github.retrooper.packetevents.protocol.player.EquipmentSlot.valueOf(equipmentSlot.name()),
                        new ItemStack.Builder().type(ItemTypes.AIR).build());
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void reloadInventory() {
        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(this.followerType);
        if (followerHandler == null) {
            despawn();
            return;
        }

        for (EquipmentSlot equipmentSlot : followerHandler.getEquipment().keySet()) {
            try {
                setArmorSlot(equipmentSlot, followerHandler);
            } catch (IllegalArgumentException ignored) {}
        }

        if (entity != null) {
            entity.getEntityMeta().setInvisible(!followerHandler.isVisible());
        }
    }

    public boolean hasTask(String id) {
        return tasks.contains(id);
    }

    public void addTasks(String... ids) {
        for (String id : ids) {
            addTask(id);
        }
    }

    public void addTask(String id) {
        tasks.add(id);
    }

    public void removeTask(String id) {
        if (id.equals(TaskId.ALL)) {
            tasks.clear();
        } else {
            tasks.remove(id);
        }
    }

    public void removeTasks(String... ids) {
        for (String id : ids) {
            removeTask(id);
        }
    }

    public void refresh() {
        if (entity == null) {
            return;
        }

        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerType);
        if (followerHandler == null) {
            return;
        }

        WrapperLivingEntity entity;
        if (!this.entity.getEntityType().equals(followerHandler.getEntityType())) {
            // Handles changing the entity type
            entity = followerHandler.createEntity(this.entity.getEntityId(), this.entity.getUuid());

            if (entity instanceof WrapperPlayer wrapperPlayer) {
                SkinData skinData = followerHandler.getSkin();
                if (skinData != null && skinData.getValue().equals("mirror")) {
                    if (this instanceof OwnedFollower ownedFollower && ownedFollower.getOwner() instanceof Player player) {
                        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                        wrapperPlayer.setTextureProperties(user.getProfile().getTextureProperties());
                    }
                }
            }

            this.entity.despawn();
            entity.spawn(this.entity.getLocation());
        } else {
            entity = this.entity;
        }

        followerHandler.applyAttributes(entity);
        this.entity = entity;
        reloadInventory();

        refreshNametag();
    }

    public void refreshNametag() {
        if (entity == null || !entity.isSpawned()) {
            if (nameTagEntity != null && nameTagEntity.isSpawned()) {
                nameTagEntity.despawn();
            }

            return;
        }

        if (nameTagEntity == null) {
            if (displayName != null) {
                nameTagEntity = summonNameTagEntity();
                if (nameTagEntity == null) {
                    return;
                }
            } else {
                return;
            }
        } else if (!nameTagEntity.isSpawned()) {
            nameTagEntity.spawn(new Location(entity.getLocation().getPosition(), 0, 0));
        }

        float translation;
        if (entity != null && entity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta && armorStandMeta.isMarker()) {
            translation = 0.6f;
        } else {
            translation = 0.1f;
        }

        TextDisplayMeta textDisplayMeta = (TextDisplayMeta) nameTagEntity.getEntityMeta();
        textDisplayMeta.setTranslation(new Vector3f(0, translation, 0));

        float scale = (float) getTypeHandler().getScale() + 0.25f;
        textDisplayMeta.setScale(new Vector3f(scale, scale, scale));

        String nickname = Followers.getInstance().getConfigManager().getFollowerNicknameFormat()
            .replaceAll("%nickname%", displayName);

        textDisplayMeta.setText(ModernChatColorHandler.translate(nickname));

        if (!entity.hasPassenger(nameTagEntity)) {
            entity.addPassenger(nameTagEntity.getEntityId());
        }
    }

    public boolean spawn() {
        if (this.getWorld() == null) {
            throw new IllegalStateException("World must be defined to spawn follower.");
        }

        return spawn(this.getWorld(), new Location(getTarget(), 0, 0));
    }

    public boolean spawn(@NotNull World world, @NotNull Location location) {
        if (isSpawned()) {
            throw new IllegalStateException("Follower is already spawned.");
        }

        this.setWorld(world);

        if (Followers.getInstance().callEvent(new FollowerEntitySpawnEvent(this))) {
            FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerType);
            if (followerHandler == null) {
                return false;
            }

            entity = followerHandler.createEntity();

            if (entity instanceof WrapperPlayer wrapperPlayer) {
                SkinData skinData = followerHandler.getSkin();
                if (skinData != null && skinData.getValue().equals("mirror")) {
                    if (this instanceof OwnedFollower ownedFollower && ownedFollower.getOwner() instanceof Player player) {
                        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                        wrapperPlayer.setTextureProperties(user.getProfile().getTextureProperties());
                    }
                }
            }


            EntityLib.getApi().spawnEntity(entity, location);
            refresh();
            setType(followerType);

            addTasks(
                TaskId.MOVE_NEAR,
                TaskId.VIEWERS,
                TaskId.VALIDATE
            );

            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::reloadInventory, 5);
            return true;
        } else {
            despawn();
            return false;
        }
    }

    public void despawn() {
        removeTasks(
            TaskId.MOVE_NEAR,
            TaskId.PARTICLE,
            TaskId.PARTICLE_CLOUD,
            TaskId.VIEWERS,
            TaskId.VALIDATE
        );

        if (entity != null) {
            entity.despawn();
        }

        if (nameTagEntity != null) {
            nameTagEntity.despawn();
        }
    }

    public void remove() {
        removeTasks(TaskId.MOVE_NEAR, TaskId.PARTICLE, TaskId.VALIDATE);

        if (entity != null) {
            entity.remove();
            entity = null;
        }

        if (nameTagEntity != null) {
            nameTagEntity.remove();
            nameTagEntity = null;
        }
    }

    private WrapperEntity summonNameTagEntity() {
        if (entity == null) {
            return null;
        }

        WrapperEntity textDisplay;
        try {
            textDisplay = EntityLib.getApi().createEntity(EntityTypes.TEXT_DISPLAY);
            EntityLib.getApi().spawnEntity(textDisplay, new Location(entity.getLocation().getPosition(), 0, 0));
            entity.getViewers().forEach(textDisplay::addViewer);
            entity.addPassenger(textDisplay);

            TextDisplayMeta textDisplayMeta = (TextDisplayMeta) textDisplay.getEntityMeta();
            textDisplayMeta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
            textDisplayMeta.setBackgroundColor(0);
            textDisplayMeta.setShadow(true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return textDisplay;
    }
}
