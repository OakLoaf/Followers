package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.mobs.horse.CamelMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class CamelPoses extends PoseSet {

    public CamelPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(CamelMeta.class).setLastPoseChangeTick(0);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(CamelMeta.class).setLastPoseChangeTick(-30);
        });
    }
}
