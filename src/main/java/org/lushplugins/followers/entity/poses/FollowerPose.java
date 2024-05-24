package org.lushplugins.followers.entity.poses;

import com.github.retrooper.packetevents.util.Vector3f;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

public enum FollowerPose {
    DEFAULT(new PoseData()
            .setLeftArmPose(new Vector3f(-0.17453292519943295f, 0, -0.17453292519943295f))
            .setRightArmPose(new Vector3f(-0.2617993877991494f, 0, 0.17453292519943295f))
            .setLeftLegPose(new Vector3f(-0.017453292519943295f, 0, -0.017453292519943295f))
            .setRightLegPose(new Vector3f(0.017453292519943295f, 0, 0.017453292519943295f))),
    SITTING(new PoseData()
            .setLeftArmPose(new Vector3f(-0.78f, 0, -0.17453292519943295f))
            .setRightArmPose(new Vector3f(-0.78f, 0, 0.17453292519943295f))
            .setLeftLegPose(new Vector3f(4.6f, -0.222f, 0))
            .setRightLegPose(new Vector3f(4.6f, 0.222f, 0))),
    SPINNING(new PoseData()
            .setLeftArmPose(new Vector3f(-0.17453292519943295f, 0, -0.17453292519943295f))
            .setRightArmPose(new Vector3f(-0.2617993877991494f, 0, 0.17453292519943295f))
            .setLeftLegPose(new Vector3f(-0.017453292519943295f, 0, -0.017453292519943295f))
            .setRightLegPose(new Vector3f(0.017453292519943295f, 0, 0.017453292519943295f)));


    private final PoseData poseStack;
    FollowerPose(PoseData poseStack) { this.poseStack = poseStack; }
    public void pose(ArmorStandMeta armorStandMeta) { poseStack.pose(armorStandMeta); }
}
