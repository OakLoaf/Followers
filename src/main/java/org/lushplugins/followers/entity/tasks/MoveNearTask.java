package org.lushplugins.followers.entity.tasks;

import com.github.retrooper.packetevents.util.Vector3d;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.Follower;

public class MoveNearTask extends MoveToTask {

    public MoveNearTask(String id) {
        super(id);
    }

    @Override
    public Vector3d calculatePosition(Follower follower) {
        WrapperLivingEntity entity = follower.getEntity();
        double speed = Followers.getInstance().getConfigManager().getSpeed();

        // Calculates new location of entity based off of the distance to the player
        Vector3d position = entity.getLocation().getPosition();
        Vector3d difference = getDifference(position, follower.getTarget()); // TODO: Work out how to get entity eye location from entity
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

        // Adds y offset of entity (Bobbing animation)
        position = position.add(0, calculateYOffset(entity), 0);

        return position;
    }
}
