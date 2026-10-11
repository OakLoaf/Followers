package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.MetaFields;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class PiglinPoses extends PoseSet {

    public PiglinPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.setMetadata(MetaFields.Piglin.IS_DANCING, false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.setMetadata(MetaFields.Piglin.IS_DANCING, false);
        });
    }
}
