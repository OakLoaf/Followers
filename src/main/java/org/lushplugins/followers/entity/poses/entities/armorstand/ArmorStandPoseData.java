package org.lushplugins.followers.entity.poses.entities.armorstand;

import com.github.retrooper.packetevents.util.Vector3f;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;

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

    public void pose(ArmorStandMeta armorStandMeta) {
        if (headPose != null) {
            armorStandMeta.setHeadRotation(headPose);
        }

        if (bodyPose != null) {
            armorStandMeta.setBodyRotation(bodyPose);
        }

        if (leftArmPose != null) {
            armorStandMeta.setLeftArmRotation(leftArmPose);
        }

        if (rightArmPose != null) {
            armorStandMeta.setRightArmRotation(rightArmPose);
        }

        if (leftLegPose != null) {
            armorStandMeta.setLeftLegRotation(leftLegPose);
        }

        if (rightLegPose != null) {
            armorStandMeta.setRightLegRotation(rightLegPose);
        }
    }
}
