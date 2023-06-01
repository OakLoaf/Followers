package me.dave.followers.entity.pose;

import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

public enum FollowerPose {
    DEFAULT(new PoseStack()
            .setLeftArmPose(new EulerAngle(-0.17453292519943295, 0, -0.17453292519943295))
            .setRightArmPose(new EulerAngle(-0.2617993877991494, 0, 0.17453292519943295))
            .setLeftLegPose(new EulerAngle(-0.017453292519943295, 0, -0.017453292519943295))
            .setRightLegPose(new EulerAngle(0.017453292519943295, 0, 0.017453292519943295))),
    SITTING(new PoseStack()
            .setLeftArmPose(new EulerAngle(-0.78, 0, -0.17453292519943295))
            .setRightArmPose(new EulerAngle(-0.78, 0, 0.17453292519943295))
            .setLeftLegPose(new EulerAngle(4.6, -0.222, 0))
            .setRightLegPose(new EulerAngle(4.6, 0.222, 0))),
    SPINNING(new PoseStack()
            .setLeftArmPose(new EulerAngle(-0.17453292519943295, 0, -0.17453292519943295))
            .setRightArmPose(new EulerAngle(-0.2617993877991494, 0, 0.17453292519943295))
            .setLeftLegPose(new EulerAngle(-0.017453292519943295, 0, -0.017453292519943295))
            .setRightLegPose(new EulerAngle(0.017453292519943295, 0, 0.017453292519943295)));


    private final PoseStack poseStack;
    FollowerPose(PoseStack poseStack) { this.poseStack = poseStack; }
    public void pose(ArmorStand armorStand) { poseStack.pose(armorStand); }
}
