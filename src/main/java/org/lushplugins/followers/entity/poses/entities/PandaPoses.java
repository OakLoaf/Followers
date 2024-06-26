package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.mobs.PandaMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class PandaPoses extends PoseSet {

    public PandaPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(PandaMeta.class).setOnBack(false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(PandaMeta.class).setOnBack(true);
        });
    }
}
