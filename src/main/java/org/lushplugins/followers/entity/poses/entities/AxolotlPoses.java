package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.mobs.water.AxolotlMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class AxolotlPoses extends PoseSet {

    public AxolotlPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(AxolotlMeta.class).setPlayingDead(false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(AxolotlMeta.class).setPlayingDead(true);
        });
    }
}
