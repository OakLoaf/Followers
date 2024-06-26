package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.mobs.monster.piglin.PiglinMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class PiglinPoses extends PoseSet {

    public PiglinPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(PiglinMeta.class).setDancing(false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(PiglinMeta.class).setDancing(true);
        });
    }
}
