package org.lushplugins.followers.entity.tasks;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;
import org.bukkit.entity.Player;
import org.lushplugins.lushlib.utils.Pair;

public class MovementTask extends FollowerTask {
    private final Player player;
    private final double speed;

    public MovementTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
        this.speed = Followers.getInstance().getConfigManager().getSpeed();
    }

    @Override
    public void tick() {
        // Cancels the task if the entity is dead
        if (!followerEntity.isEntityValid()) {
            cancel();
            return;
        }

        WrapperLivingEntity entity = followerEntity.getEntity();
        Location currentLocation = entity.getLocation();
        Vector3d newPosition = calculatePosition();

        float yaw;
        float pitch;
        // Limits following code to run once every 2 ticks
        if (Followers.getInstance().getCurrentTick() % 2 == 0) {
            Pair<Float, Float> rotation = calculateRotation();
            yaw = rotation.first();
            pitch = rotation.second();
        } else {
            yaw = currentLocation.getYaw();
            pitch = currentLocation.getPitch();
        }

        Location newLocation = new Location(newPosition, yaw, pitch);

        // Teleports follower
        boolean tpSuccess = followerEntity.teleport(newLocation);
        if (!tpSuccess) {
            Followers.getInstance().getDataManager().getFollowerUser(player).respawnFollowerEntity();
        }

        // Limits following code to run once every 2 ticks
        if (Followers.getInstance().getCurrentTick() % 2 == 0) {
            if (entity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta) {
                // Sets follower head to be looking at the player
                double headPoseX = eulerToDegree(armorStandMeta.getHeadRotation().getX());
                Vector3f newHeadPoseX;
                if (headPoseX > 60 && headPoseX < 290) {
                    newHeadPoseX = new Vector3f(headPoseX <= 175 ? 60 : 290, 0, 0);
                } else {
                    newHeadPoseX = new Vector3f(getPitch(player, entity), 0, 0);
                }

                armorStandMeta.setHeadRotation(newHeadPoseX);
            }
        }
    }

    public Vector3d calculatePosition() {
        WrapperLivingEntity entity = followerEntity.getEntity();
        Vector3d position = entity.getLocation().getPosition();
        Vector3d difference = getDifference(player, entity);

        // Calculates new location of entity based off of the distance to the player
        if (new Vector3d(difference.getX(), 0 , difference.getZ()).lengthSquared() < 6.25) {
            double differenceY = difference.getY() - (Followers.getInstance().getConfigManager().areHitboxesEnabled() ? 0.25 : 0.7);
            position = position.add(new Vector3d(0, differenceY * speed, 0));
        } else {
            Vector3d normalizedDifference = difference.normalize();
            double distance = difference.length() - 5;
            if (distance < 1) {
                distance = 1;
            }

            position = position.add(normalizedDifference.multiply(speed * distance));
        }

        return position.add(0, calculateYOffset(entity), 0);
    }

    /**
     * Calculate the head rotation of the entity
     * @return A pair of yaw and pitch as floats
     */
    // TODO: Consider changing to return Vector3f(pitch, yaw?, roll?)
    public Pair<Float, Float> calculateRotation() {
        WrapperLivingEntity entity = followerEntity.getEntity();

        float pitch = getPitch(player, entity);
        return new Pair<>(0f, pitch);
    }

    @Override
    public String getIdentifier() {
        return TaskId.MOVEMENT;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public int getPeriod() {
        return 1;
    }

    private static double calculateYOffset(WrapperLivingEntity entity) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Followers.getInstance().getCurrentTick() + entity.getEntityId()));
    }

    private static float getPitch(Player player, WrapperLivingEntity entity) {
        // TODO: Work out how to getEyeLocation
        // Vector difference = (player.getEyeLocation().subtract(0,0.9, 0)).subtract(entity.getEyeLocation()).toVector();
        Vector3d difference = SpigotConversionUtil.fromBukkitLocation(player.getEyeLocation().subtract(0, 0.9, 0)).getPosition().subtract(entity.getLocation().getPosition());
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) {
            return (float) (difference.getY() > 0.0D ? -90 : 90);
        } else {
            return (float) Math.atan(-difference.getY() / Math.sqrt((difference.getX()*difference.getX()) + (difference.getZ()*difference.getZ())));
        }
    }

    private static Vector3d getDifference(Player player, WrapperLivingEntity entity) {
        // TODO: Work out how to getEyeLocation
        // Vector vector = player.getEyeLocation().subtract(entity.getEyeLocation()).toVector();
        return SpigotConversionUtil.fromBukkitLocation(player.getEyeLocation()).getPosition().subtract(entity.getLocation().getPosition());
    }

    private static double eulerToDegree(double euler) {
        return (euler / (2 * Math.PI)) * 360;
    }
}
