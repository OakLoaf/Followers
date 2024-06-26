package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.mobs.tameable.ParrotMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class ParrotPoses extends PoseSet {

    public ParrotPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(ParrotMeta.class).setSitting(false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(ParrotMeta.class).setSitting(true);
        });
    }
}
