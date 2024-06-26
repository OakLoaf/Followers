package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.mobs.tameable.WolfMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class WolfPoses extends PoseSet {

    public WolfPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(WolfMeta.class).setSitting(false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(WolfMeta.class).setSitting(true);
        });
    }
}
