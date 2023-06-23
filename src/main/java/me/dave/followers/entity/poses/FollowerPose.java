package me.dave.followers.entity.poses;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

public enum FollowerPose {
    DEFAULT(new PoseData()
            .setLeftArmPose(new EulerAngle(-0.17453292519943295, 0, -0.17453292519943295))
            .setRightArmPose(new EulerAngle(-0.2617993877991494, 0, 0.17453292519943295))
            .setLeftLegPose(new EulerAngle(-0.017453292519943295, 0, -0.017453292519943295))
            .setRightLegPose(new EulerAngle(0.017453292519943295, 0, 0.017453292519943295))),
    SITTING(new PoseData()
            .setLeftArmPose(new EulerAngle(-0.78, 0, -0.17453292519943295))
            .setRightArmPose(new EulerAngle(-0.78, 0, 0.17453292519943295))
            .setLeftLegPose(new EulerAngle(4.6, -0.222, 0))
            .setRightLegPose(new EulerAngle(4.6, 0.222, 0))),
    SPINNING(new PoseData()
            .setLeftArmPose(new EulerAngle(-0.17453292519943295, 0, -0.17453292519943295))
            .setRightArmPose(new EulerAngle(-0.2617993877991494, 0, 0.17453292519943295))
            .setLeftLegPose(new EulerAngle(-0.017453292519943295, 0, -0.017453292519943295))
            .setRightLegPose(new EulerAngle(0.017453292519943295, 0, 0.017453292519943295)));


    private final PoseData poseStack;
    FollowerPose(PoseData poseStack) { this.poseStack = poseStack; }
    public void pose(ArmorStand armorStand) { poseStack.pose(armorStand); }
}
