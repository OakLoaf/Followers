package org.lushplugins.followers.entity.tasks;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.Follower;

public class MoveToTask extends FollowerTask {

    public MoveToTask(String id) {
        super(id);
    }

    @Override
    public void tick(Follower follower) {
        // Cancels the task if the entity is dead
        if (!follower.isEntityValid()) {
            follower.removeTask(this.getId());
            return;
        }

        WrapperLivingEntity entity = follower.getEntity();
        Location currentLocation = entity.getLocation();
        Vector3d newPosition = calculatePosition(follower);

        // Calculations limited to running once every 2 ticks
        Vector3f rotation;
        if (Followers.getInstance().getCurrentTick() % 2 == 0) {
            rotation = calculateRotation(follower);
        } else {
            rotation = new Vector3f(currentLocation.getPitch(), currentLocation.getYaw(), 0);
        }

        follower.teleport(new Location(newPosition, rotation.getY(), rotation.getX()));
        entity.rotateHead(rotation.getY(), rotation.getX());

        if (entity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta) {
            Vector3f rotationRadians = new Vector3f((float) Math.toRadians(rotation.getX()), (float) Math.toRadians(rotation.getY()), (float) Math.toRadians(rotation.getZ()));
            armorStandMeta.setHeadRotation(rotationRadians);
        }
    }

    public Vector3d calculatePosition(Follower follower) {
        WrapperLivingEntity entity = follower.getEntity();
        double speed = Followers.getInstance().getConfigManager().getSpeed();

        // Calculates new location of entity based off of the distance to the player
        Vector3d position = entity.getLocation().getPosition();
        Vector3d target = follower.getTarget();
        if (target != null) {
            Vector3d difference = getDifference(position, follower.getTarget()); // TODO: Work out how to get entity eye location from entity
            Vector3d normalizedDifference = difference.normalize();
            double distance = difference.length() - 5;
            if (distance < 1) {
                distance = 1;
            }

            position = position.add(normalizedDifference.multiply(speed * distance));
        }

        // Adds y offset of entity (Bobbing animation)
        position = position.add(0, calculateYOffset(entity), 0);

        return position;
    }

    /**
     * Calculate the rotation of the entity
     * @return A rotation vector of pitch, yaw and roll
     */
    public Vector3f calculateRotation(Follower follower) {
        WrapperLivingEntity entity = follower.getEntity();

        // TODO: Work out how to get entity eye location from entity
        Vector3d difference = getDifference(entity.getLocation().getPosition(), follower.getTarget());
        float pitch = getPitch(difference);
        if (pitch > 60 && pitch < 290) {
            pitch = pitch <= 175 ? 60 : 290;
        }
        float yaw = getYaw(difference);

        return new Vector3f(pitch, yaw, 0);
    }

    @Override
    public int getPeriod() {
        return 1;
    }

    protected static double calculateYOffset(WrapperLivingEntity entity) {
        return (Math.PI / 60) * Math.sin(((double) 1/30) * Math.PI * (Followers.getInstance().getCurrentTick() + entity.getEntityId()));
    }

    protected static float getYaw(Vector3d difference) {
        double yawRadians = Math.atan2(difference.getX(), difference.getZ());
        return (float) -Math.toDegrees(yawRadians);
    }

    protected static float getPitch(Vector3d difference) {
        if (difference.getX() == 0.0D && difference.getZ() == 0.0D) {
            return (float) (difference.getY() > 0.0D ? -90 : 90);
        } else {
            double pitchRadians = Math.atan(-difference.getY() / Math.sqrt((difference.getX() * difference.getX()) + (difference.getZ() * difference.getZ())));
            return (float) Math.toDegrees(pitchRadians);
        }
    }

    protected static Vector3d getDifference(Vector3d from, Vector3d to) {
        return to.subtract(from);
    }
}
