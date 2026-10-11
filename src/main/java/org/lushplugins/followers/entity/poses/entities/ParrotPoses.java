package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.MetaFields;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class ParrotPoses extends PoseSet {

    public ParrotPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.setFlag(MetaFields.Parrot.SITTING, false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.setFlag(MetaFields.Parrot.SITTING, true);
        });
    }
}
