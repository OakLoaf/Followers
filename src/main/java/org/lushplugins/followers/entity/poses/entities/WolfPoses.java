package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.MetaFields;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class WolfPoses extends PoseSet {

    public WolfPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.setFlag(MetaFields.Wolf.SITTING, false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.setFlag(MetaFields.Wolf.SITTING, true);
        });
    }
}
