package org.lushplugins.followers.entity.poses.entities;

import me.tofaa.entitylib.meta.MetaFields;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class CatPoses extends PoseSet {

    public CatPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.setMetadata(MetaFields.Cat.IS_LYING, false);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.setMetadata(MetaFields.Cat.IS_LYING, true);
        });
    }
}
