package org.lushplugins.followers.entity.poses.entities.armorstand;

import com.github.retrooper.packetevents.util.Vector3f;
import me.tofaa.entitylib.meta.MetaFields;
import me.tofaa.entitylib.wrapper.WrapperEntity;

public class ArmorStandPoseData {
    private Vector3f headPose;
    private Vector3f bodyPose;
    private Vector3f leftArmPose;
    private Vector3f rightArmPose;
    private Vector3f leftLegPose;
    private Vector3f rightLegPose;

    public ArmorStandPoseData setHeadPose(Vector3f vector) {
        this.headPose = vector;
        return this;
    }

    public ArmorStandPoseData setBodyPose(Vector3f vector) {
        this.bodyPose = vector;
        return this;
    }

    public ArmorStandPoseData setLeftArmPose(Vector3f vector) {
        this.leftArmPose = vector;
        return this;
    }

    public ArmorStandPoseData setRightArmPose(Vector3f vector) {
        this.rightArmPose = vector;
        return this;
    }

    public ArmorStandPoseData setLeftLegPose(Vector3f vector) {
        this.leftLegPose = vector;
        return this;
    }

    public ArmorStandPoseData setRightLegPose(Vector3f vector) {
        this.rightLegPose = vector;
        return this;
    }

    public void pose(WrapperEntity entity) {
        if (headPose != null) {
            entity.setMetadata(MetaFields.ArmorStand.HEAD_POSE, headPose);
        }

        if (bodyPose != null) {
            entity.setMetadata(MetaFields.ArmorStand.BODY_POSE, bodyPose);
        }

        if (leftArmPose != null) {
            entity.setMetadata(MetaFields.ArmorStand.LEFT_ARM_POSE, leftArmPose);
        }

        if (rightArmPose != null) {
            entity.setMetadata(MetaFields.ArmorStand.RIGHT_ARM_POSE, rightArmPose);
        }

        if (leftLegPose != null) {
            entity.setMetadata(MetaFields.ArmorStand.LEFT_LEG_POSE, leftLegPose);
        }

        if (rightLegPose != null) {
            entity.setMetadata(MetaFields.ArmorStand.RIGHT_LEG_POSE, rightLegPose);
        }
    }
}
