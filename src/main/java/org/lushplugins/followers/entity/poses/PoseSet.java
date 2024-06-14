package org.lushplugins.followers.entity.poses;

import me.tofaa.entitylib.wrapper.WrapperLivingEntity;

import java.util.HashMap;
import java.util.function.Consumer;

public class PoseSet {
    private final HashMap<FollowerPose, Consumer<WrapperLivingEntity>> poses = new HashMap<>();

    public void applyPose(FollowerPose pose, WrapperLivingEntity entity) {
        if (poses.containsKey(pose)) {
            poses.get(pose).accept(entity);
        }
    }

    public void addPose(FollowerPose pose, Consumer<WrapperLivingEntity> consumer) {
        poses.put(pose, consumer);
    }

    public void removePose(FollowerPose pose) {
        poses.remove(pose);
    }
}
