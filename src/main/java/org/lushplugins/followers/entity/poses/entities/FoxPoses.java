package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.MetaFields;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class FoxPoses extends PoseSet {

    public FoxPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.setFlag(MetaFields.Fox.SLEEPING, false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.setFlag(MetaFields.Fox.SLEEPING, true);
        });
    }
}
