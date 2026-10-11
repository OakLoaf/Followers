package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.MetaFields;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class PandaPoses extends PoseSet {

    public PandaPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.setFlag(MetaFields.Panda.ON_BACK, false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.setFlag(MetaFields.Panda.ON_BACK, true);
        });
    }
}
