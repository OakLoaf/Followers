package org.lushplugins.followers.entity.tasks;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;

public class MoveToTask extends FollowerTask {

    public MoveToTask(FollowerEntity followerEntity) {
        super(followerEntity);
    }

    @Override
    public void tick() {
        // Cancels the task if the entity is dead
        if (!followerEntity.isEntityValid()) {
            cancel();
            return;
        }

        // TODO: Replace player with target entity (Potentially implement as separate task)
        followerEntity.setTarget(SpigotConversionUtil.fromBukkitLocation(followerEntity.getPlayer().getLocation()));

        WrapperLivingEntity entity = followerEntity.getEntity();
        Location currentLocation = entity.getLocation();
        Vector3d newPosition = calculatePosition();

        // Calculations limited to running once every 2 ticks
        Vector3f rotation;
        if (Followers.getInstance().getCurrentTick() % 2 == 0) {
            rotation = calculateRotation();
        } else {
            rotation = new Vector3f(currentLocation.getPitch(), currentLocation.getYaw(), 0);
        }

        followerEntity.teleport(new Location(newPosition, rotation.getY(), rotation.getX()));
        entity.rotateHead(rotation.getY(), rotation.getX());

        if (entity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta) {
            Vector3f rotationRadians = new Vector3f((float) Math.toRadians(rotation.getX()), (float) Math.toRadians(rotation.getY()), (float) Math.toRadians(rotation.getZ()));
            armorStandMeta.setHeadRotation(rotationRadians);
        }
    }

    public Vector3d calculatePosition() {
        WrapperLivingEntity entity = followerEntity.getEntity();
        double speed = Followers.getInstance().getConfigManager().getSpeed();

        // Calculates new location of entity based off of the distance to the player
        Vector3d position = entity.getLocation().getPosition();
        Vector3d difference = getDifference(position, followerEntity.getTarget()); // TODO: Work out how to get entity eye location from entity
        Vector3d normalizedDifference = difference.normalize();
        double distance = difference.length() - 5;
        if (distance < 1) {
            distance = 1;
        }

        position = position
            .add(normalizedDifference.multiply(speed * distance))
            .add(0, calculateYOffset(entity), 0); // Adds y offset of entity (Bobbing animation)

        return position;
    }

    /**
     * Calculate the rotation of the entity
     * @return A rotation vector of pitch, yaw and roll
     */
    public Vector3f calculateRotation() {
        WrapperLivingEntity entity = followerEntity.getEntity();

        // TODO: Work out how to get entity eye location from entity
        Vector3d difference = getDifference(entity.getLocation().getPosition(), followerEntity.getTarget());
        float pitch = getPitch(difference);
        if (pitch > 60 && pitch < 290) {
            pitch = pitch <= 175 ? 60 : 290;
        }
        float yaw = getYaw(difference);

        return new Vector3f(pitch, yaw, 0);
    }

    @Override
    public String getIdentifier() {
        return TaskId.MOVE_TO;
    }

    @Override
    public int getPeriod() {
        return 1;
    }

    private static double calculateYOffset(WrapperLivingEntity entity) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Followers.getInstance().getCurrentTick() + entity.getEntityId()));
    }

    private static float getYaw(Vector3d difference) {
        double yawRadians = Math.atan2(difference.getX(), difference.getZ());
        return (float) -Math.toDegrees(yawRadians);
    }

    private static float getPitch(Vector3d difference) {
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) {
            return (float) (difference.getY() > 0.0D ? -90 : 90);
        } else {
            double pitchRadians = Math.atan(-difference.getY() / Math.sqrt((difference.getX() * difference.getX()) + (difference.getZ() * difference.getZ())));
            return (float) Math.toDegrees(pitchRadians);
        }
    }

    private static Vector3d getDifference(Vector3d from, Vector3d to) {
        return to.subtract(from);
    }
}
