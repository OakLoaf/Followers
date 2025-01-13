package org.lushplugins.followers.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBundle;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
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

import org.lushplugins.followers.utils.SkinData;
import org.lushplugins.followers.utils.PacketsHelper;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;
import org.lushplugins.lushlib.libraries.chatcolor.parsers.ParserTypes;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Follower {
    private final HashSet<String> tasks = new HashSet<>();
    private String followerType;
    private WrapperEntity entity;
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

    public String getTypeName() {
        return followerType;
    }

    public FollowerHandler getType() {
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

    public @Nullable WrapperEntity getEntity() {
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

    public void refreshHandler() {
        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(this.followerType);
        if (followerHandler == null) {
            despawn();
            return;
        }

        followerHandler.applyAttributes(entity);
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
            this.entity.sendPacketToViewers(new WrapperPlayServerBundle());
            // Despawn current entity
            this.entity.despawn();
            // Handles changing the entity type
            entity = followerHandler.createEntity(this.entity.getEntityId(), this.entity.getUuid());
            // Spawn new entity
            entity.spawn(this.entity.getLocation());
            this.entity.sendPacketToViewers(new WrapperPlayServerBundle());
        } else {
            entity = this.entity;

            if (entity instanceof WrapperPlayer wrapperPlayer) {
                SkinData skinData = followerHandler.getSkin();
                if (skinData != null) {
                    List<TextureProperty> textureProperties;
                    if (skinData.getValue().equals("mirror")) {
                        if (this instanceof OwnedFollower ownedFollower && ownedFollower.getOwner() instanceof Player player) {
                            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                            textureProperties = user.getProfile().getTextureProperties();
                        } else {
                            textureProperties = Collections.emptyList();
                        }
                    } else {
                        textureProperties = List.of(new TextureProperty("textures", skinData.getValue(), skinData.getSignature()));
                    }

                    wrapperPlayer.setTextureProperties(textureProperties);
                }
            }
        }

        followerHandler.applyAttributes(entity);
        this.entity = entity;
        refreshHandler();

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

        float scale = (float) getType().getScale() + 0.25f;
        float translation;
        if (entity != null && entity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta && armorStandMeta.isMarker()) {
            translation = 1.2f * (scale - 0.25f);
        } else {
            translation = 0.1f;
        }

        TextDisplayMeta textDisplayMeta = (TextDisplayMeta) nameTagEntity.getEntityMeta();
        textDisplayMeta.setTranslation(new Vector3f(0, translation, 0));

        textDisplayMeta.setScale(new Vector3f(scale, scale, scale));

        String nickname = Followers.getInstance().getConfigManager().getFollowerNicknameFormat()
            .replaceAll("%nickname%", displayName);

        textDisplayMeta.setText(ModernChatColorHandler.translate(nickname, ParserTypes.color()));

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

        if (!Followers.getInstance().callEvent(new FollowerEntitySpawnEvent(this))) {
            despawn();
            return false;
        }

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

        entity.spawn(location);
        refresh();
        setType(followerType);

        if (!(entity instanceof WrapperPlayer)) {
            PacketsHelper.sendPacket(
                PacketsHelper.createTeamsAddEntitiesPacket(
                    PacketsHelper.FOLLOWERS_TEAM_NAME,
                    Collections.singletonList(entity.getUuid().toString())
                ),
                Bukkit.getOnlinePlayers()
            );
        }

        addTasks(
            TaskId.MOVE_NEAR,
            TaskId.VIEWERS,
            TaskId.VALIDATE
        );

        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::refreshHandler, 5);
        return true;
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
            textDisplay = new WrapperEntity(EntityTypes.TEXT_DISPLAY);
            textDisplay.spawn(new Location(entity.getLocation().getPosition(), 0, 0));
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
