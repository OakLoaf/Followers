package org.lushplugins.followers.entity.poses.entities;

import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState;
import me.tofaa.entitylib.meta.mobs.SnifferMeta;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class SnifferPoses extends PoseSet {

    public SnifferPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.getEntityMeta(SnifferMeta.class).setState(SnifferState.IDLING);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.getEntityMeta(SnifferMeta.class).setState(SnifferState.FEELING_HAPPY);
        });
    }
}
