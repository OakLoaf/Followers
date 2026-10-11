package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.MetaFields;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class CamelPoses extends PoseSet {

    public CamelPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.setMetadata(MetaFields.Camel.LAST_POSE_CHANGE_TICK, 0L);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.setMetadata(MetaFields.Camel.LAST_POSE_CHANGE_TICK, -30L);
        });
    }
}
