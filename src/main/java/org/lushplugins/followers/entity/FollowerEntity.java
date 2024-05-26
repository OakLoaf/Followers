package org.lushplugins.followers.entity;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.world.Location;
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
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.FollowerEntityChangeTypeEvent;
import org.lushplugins.followers.api.events.FollowerEntitySpawnEvent;
import org.lushplugins.followers.api.events.FollowerEntityTickEvent;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.data.FollowerHandler;
import org.bukkit.entity.Player;
import org.lushplugins.followers.data.FollowerUser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.entity.tasks.*;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public class FollowerEntity {
    private final ConcurrentHashMap<String, FollowerTask> tasks = new ConcurrentHashMap<>();
    private final Player player;
    private WrapperLivingEntity bodyEntity;
    private WrapperEntity nametagEntity;
    private String followerType;
    private boolean alive;
    private boolean visible;
    private FollowerPose pose;

    public FollowerEntity(Player player, String followerType) {
        this.player = player;
        this.followerType = followerType;
        this.visible = !player.isInvisible();
        this.alive = false;
    }

    public Player getPlayer() {
        return player;
    }

    public WrapperLivingEntity getEntity() {
        return bodyEntity;
    }

    public boolean isEntityValid() {
        return bodyEntity != null;
    }

    @Nullable
    public Location getLocation() {
        return bodyEntity != null ? bodyEntity.getLocation().clone() : null;
    }

    public boolean isAlive() {
        if (!isEntityValid()) {
            alive = false;
        }

        return alive;
    }

    public FollowerHandler getType() {
        return Followers.getInstance().getFollowerManager().getFollower(followerType);
    }

    public void setType(String followerType) {
        if (Followers.getInstance().callEvent(new FollowerEntityChangeTypeEvent(this, this.followerType, followerType))) {
            this.followerType = followerType;

            Followers.getInstance().getDataManager().getFollowerUser(player).setFollowerType(followerType);
            if (!player.isInvisible()) {
                reloadInventory();
            }
        }
    }

    public String getDisplayName() {
        return Followers.getInstance().getDataManager().getFollowerUser(player).getDisplayName();
    }

    public void setDisplayName(String newName) {
        showDisplayName(true);

        if (Followers.getInstance().getConfigManager().areHitboxesEnabled()) {
            if (isEntityValid()) {
                bodyEntity.getEntityMeta().setCustomName(ModernChatColorHandler.translate(Followers.getInstance().getConfigManager().getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
            }
        } else {
            if (nametagEntity != null) {
                nametagEntity.getEntityMeta().setCustomName(ModernChatColorHandler.translate(Followers.getInstance().getConfigManager().getFollowerNicknameFormat().replaceAll("%nickname%", newName)));
            }
        }
    }

    public boolean isDisplayNameVisible() {
        return Followers.getInstance().getDataManager().getFollowerUser(player).isDisplayNameEnabled();
    }

    public void showDisplayName(boolean visible) {
        Followers.getInstance().getDataManager().getFollowerUser(player).setDisplayNameEnabled(visible);
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

        if (isEntityValid() && bodyEntity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta) {
            pose.pose(armorStandMeta);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        FollowerHandler followerConfig = Followers.getInstance().getFollowerManager().getFollower(followerType);
        if (followerConfig == null) {
            return;
        }

        if (isEntityValid()) {
            bodyEntity.getEntityMeta().setInvisible(!followerConfig.isVisible() || !visible);
        }

        displayName(visible && Followers.getInstance().getDataManager().getFollowerUser(player).isDisplayNameEnabled());

        if (visible) {
            reloadInventory();
        } else {
            clearInventory();
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
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.disableFollowerEntity();
            return;
        }

        if (player.isInvisible()) {
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
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);

            if (isEntityValid()) {
                bodyEntity.remove();
            }

            this.bodyEntity = summonBodyEntity();
            if (!isEntityValid()) {
                kill();
                return false;
            }

            this.alive = true;

            displayName(followerUser.isDisplayNameEnabled());

            followerUser.setFollowerEnabled(true);
            setType(followerType);
            setVisible(!player.isInvisible());

            startTask(FollowerTasks.getTask(ValidateTask.ID, this));
            startTask(FollowerTasks.getTask(VisibilityTask.ID, this));
            startMovement();

            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::reloadInventory, 5);
            return true;
        } else {
            kill();
            return false;
        }
    }

    public boolean teleport(Location location) {
        if (!isEntityValid()) {
            return false;
        }

        bodyEntity.teleport(location);
        return true;
    }

    public void kill() {
        alive = false;
        stopTasks(MovementTask.ID, ParticleTask.ID, ValidateTask.ID);

        if (bodyEntity != null) {
            bodyEntity.remove();
        }

        if (nametagEntity != null) {
            nametagEntity.remove();
        }
    }

    private void startMovement() {
        if (isEntityValid()) {
            if (player != null) {
                startTask(FollowerTasks.getTask(MovementTask.ID, this));
            }
        }
    }

    public void startParticles(ParticleType<?> particle) {
        if (isEntityValid()) {
            try {
                startTask(FollowerTasks.getClass(ParticleTask.ID).getConstructor(FollowerEntity.class, ParticleType.class).newInstance(this, particle));
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }


    ////////////////////////
    //    Task Handler    //
    ////////////////////////

    public void tick() {
        int currTick = Followers.getInstance().getCurrentTick();

        if (Followers.getInstance().callEvent(new FollowerEntityTickEvent(this))) {
            tasks.values().forEach(task -> {
                if (currTick >= task.getStartTick() && currTick % task.getPeriod() == 0) {
                    try {
                        task.tick();
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

        FollowerTask task = tasks.get(taskType);

        if (task != null) {
            if (!task.isCancelled()) {
                task.cancel();
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

            String nickname = Followers.getInstance().getConfigManager().getFollowerNicknameFormat().replaceAll("%nickname%", Followers.getInstance().getDataManager().getFollowerUser(player).getDisplayName());
            TextDisplayMeta textDisplayMeta = (TextDisplayMeta) nametagEntity.getEntityMeta();
            textDisplayMeta.setText(Component.text(nickname));
            // TODO: Fix ChatColorHandler
//            textDisplayMeta.setText(ModernChatColorHandler.translate(nickname));
        }
        else {
            if (nametagEntity != null) {
                nametagEntity.remove();
                nametagEntity = null;
            }
        }
    }

    private WrapperLivingEntity summonBodyEntity() {
        Location location = SpigotConversionUtil.fromBukkitLocation(player.getLocation().add(1.5, 0, 1.5));

        WrapperLivingEntity livingEntity;
        try {
            // TODO: Implement 'entity-type' follower option
            livingEntity = EntityLib.getApi().createEntity(EntityTypes.GOAT);
            livingEntity.spawn(location);

            // TODO: Implement 'scale' follower option
            livingEntity.getAttributes().setAttribute(Attributes.GENERIC_SCALE, 0.5);
            if (livingEntity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta) {
                armorStandMeta.setHasNoBasePlate(true);
                armorStandMeta.setHasArms(true);
                armorStandMeta.setSmall(true);

                if (!Followers.getInstance().getConfigManager().areHitboxesEnabled()) {
                    armorStandMeta.setMarker(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


        // TODO: Implement proper tracking system (not in this class)
        player.getWorld().getPlayers().forEach(viewer -> livingEntity.addViewer(viewer.getUniqueId()));
        // TODO: Remove after EntityLib fix
        livingEntity.getAttributes().refresh();

        return livingEntity;
    }

    private WrapperEntity summonNametagEntity() {
        if (!isEntityValid()) {
            return null;
        }

        WrapperEntity textDisplay;
        try {
            textDisplay = EntityLib.getApi().createEntity(EntityTypes.TEXT_DISPLAY);
            textDisplay.spawn(bodyEntity.getLocation());

            TextDisplayMeta textDisplayMeta = (TextDisplayMeta) textDisplay.getEntityMeta();
            textDisplayMeta.setTranslation(new Vector3f(0, 0.5f, 0));
            textDisplayMeta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
            textDisplayMeta.setBackgroundColor(0);
            textDisplayMeta.setShadow(true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // TODO: Implement proper tracking system (not in this class)
        player.getWorld().getPlayers().forEach(viewer -> textDisplay.addViewer(viewer.getUniqueId()));
        // TODO: Investigate passenger handling
        bodyEntity.addPassenger(textDisplay);

        return textDisplay;
    }
}
