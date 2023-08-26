package me.dave.followers.entity.poses;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

public class PoseData {
    private EulerAngle headPose;
    private EulerAngle bodyPose;
    private EulerAngle leftArmPose;
    private EulerAngle rightArmPose;
    private EulerAngle leftLegPose;
    private EulerAngle rightLegPose;

    public PoseData setHeadPose(EulerAngle eulerAngle) {
        this.headPose = eulerAngle;
        return this;
    }

    public PoseData setBodyPose(EulerAngle eulerAngle) {
        this.bodyPose = eulerAngle;
        return this;
    }

    public PoseData setLeftArmPose(EulerAngle eulerAngle) {
        this.leftArmPose = eulerAngle;
        return this;
    }

    public PoseData setRightArmPose(EulerAngle eulerAngle) {
        this.rightArmPose = eulerAngle;
        return this;
    }

    public PoseData setLeftLegPose(EulerAngle eulerAngle) {
        this.leftLegPose = eulerAngle;
        return this;
    }

    public PoseData setRightLegPose(EulerAngle eulerAngle) {
        this.rightLegPose = eulerAngle;
        return this;
    }

    public void pose(ArmorStand armorStand) {
        if (headPose != null) {
            armorStand.setHeadPose(headPose);
        }

        if (bodyPose != null) {
            armorStand.setBodyPose(bodyPose);
        }

        if (leftArmPose != null) {
            armorStand.setLeftArmPose(leftArmPose);
        }

        if (rightArmPose != null) {
            armorStand.setRightArmPose(rightArmPose);
        }

        if (leftLegPose != null) {
            armorStand.setLeftLegPose(leftLegPose);
        }

        if (rightLegPose != null) {
            armorStand.setRightLegPose(rightLegPose);
        }
    }
}
