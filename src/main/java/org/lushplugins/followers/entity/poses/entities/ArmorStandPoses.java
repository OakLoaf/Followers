package org.lushplugins.followers.entity.poses.entities;

import com.github.retrooper.packetevents.util.Vector3f;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;
import org.lushplugins.followers.entity.poses.entities.armorstand.ArmorStandPoseData;

public class ArmorStandPoses extends PoseSet {
    private static final ArmorStandPoseData DEFAULT = new ArmorStandPoseData()
        .setLeftArmPose(new Vector3f(-0.17453292519943295f, 0, -0.17453292519943295f))
        .setRightArmPose(new Vector3f(-0.2617993877991494f, 0, 0.17453292519943295f))
        .setLeftLegPose(new Vector3f(-0.017453292519943295f, 0, -0.017453292519943295f))
        .setRightLegPose(new Vector3f(0.017453292519943295f, 0, 0.017453292519943295f));
    private static final ArmorStandPoseData SITTING = new ArmorStandPoseData()
        .setLeftArmPose(new Vector3f(-0.78f, 0, -0.17453292519943295f))
        .setRightArmPose(new Vector3f(-0.78f, 0, 0.17453292519943295f))
        .setLeftLegPose(new Vector3f(4.6f, -0.222f, 0))
        .setRightLegPose(new Vector3f(4.6f, 0.222f, 0));
    private static final ArmorStandPoseData SPINNING = new ArmorStandPoseData()
        .setLeftArmPose(new Vector3f(-0.17453292519943295f, 0, -0.17453292519943295f))
        .setRightArmPose(new Vector3f(-0.2617993877991494f, 0, 0.17453292519943295f))
        .setLeftLegPose(new Vector3f(-0.017453292519943295f, 0, -0.017453292519943295f))
        .setRightLegPose(new Vector3f(0.017453292519943295f, 0, 0.017453292519943295f));

    public ArmorStandPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            DEFAULT.pose((ArmorStandMeta) entity.getEntityMeta());
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            SITTING.pose((ArmorStandMeta) entity.getEntityMeta());
        });

        addPose(FollowerPose.SPINNING, (entity) -> {
            SPINNING.pose((ArmorStandMeta) entity.getEntityMeta());
        });
    }
}
