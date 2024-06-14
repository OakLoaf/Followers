package org.lushplugins.followers.entity.poses.entities;

import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import me.tofaa.entitylib.meta.EntityMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class WardenPoses extends PoseSet {

    public WardenPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            EntityMeta meta = entity.getEntityMeta();
            if (meta.getPose() != EntityPose.STANDING) {
                entity.getEntityMeta().setPose(EntityPose.STANDING);
                entity.refresh();
            }
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            EntityMeta meta = entity.getEntityMeta();
            if (meta.getPose() != EntityPose.DIGGING) {
                entity.getEntityMeta().setPose(EntityPose.DIGGING);
                entity.refresh();
            }
        });
    }
}
