package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.mobs.FoxMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class FoxPoses extends PoseSet {

    public FoxPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(FoxMeta.class).setSleeping(false);
            entity.refresh();
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(FoxMeta.class).setSleeping(true);
            entity.refresh();
        });
    }
}
