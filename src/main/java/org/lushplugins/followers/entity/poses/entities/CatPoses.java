package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.mobs.tameable.CatMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class CatPoses extends PoseSet {

    public CatPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(CatMeta.class).setLying(false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(CatMeta.class).setLying(true);
        });
    }
}
