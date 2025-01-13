package org.lushplugins.followers.entity.poses;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.lushplugins.followers.entity.poses.entities.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.github.retrooper.packetevents.protocol.entity.type.EntityTypes.*;

public class FollowerPoseRegistry {
    private final ConcurrentHashMap<EntityType, PoseSet> entityPoses = new ConcurrentHashMap<>();

    public FollowerPoseRegistry() {
        setPoses(ARMADILLO, new ArmadilloPoses());
        setPoses(ARMOR_STAND, new ArmorStandPoses());
        setPoses(CAMEL, new CamelPoses());
        setPoses(CAT, new CatPoses());
        setPoses(FOX, new FoxPoses());
        setPoses(PANDA, new PandaPoses());
        setPoses(PARROT, new ParrotPoses());
        setPoses(PIGLIN, new PiglinPoses());
        setPoses(SNIFFER, new SnifferPoses());
        setPoses(WOLF, new WolfPoses());
    }

    public void applyPose(WrapperEntity entity, FollowerPose pose) {
        PoseSet poses = entityPoses.get(entity.getEntityType());
        if (poses != null) {
            poses.applyPose(pose, entity);
        }
    }

    public void addPose(EntityType entityType, FollowerPose pose, Consumer<WrapperEntity> consumer) {
        if (entityPoses.containsKey(entityType)) {
            entityPoses.get(entityType).addPose(pose, consumer);
        } else {
            PoseSet poses = new PoseSet();
            poses.addPose(pose, consumer);
            entityPoses.put(entityType, poses);
        }
    }

    public void removePose(EntityType entityType, FollowerPose pose) {
        if (entityPoses.containsKey(entityType)) {
            entityPoses.get(entityType).removePose(pose);
        }
    }

    public void setPoses(EntityType entityType, PoseSet poses) {
        entityPoses.put(entityType, poses);
    }
}
