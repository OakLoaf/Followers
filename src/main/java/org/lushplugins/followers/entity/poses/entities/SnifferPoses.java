package org.lushplugins.followers.entity.poses.entities;

import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState;
import me.tofaa.entitylib.meta.MetaFields;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class SnifferPoses extends PoseSet {

    public SnifferPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            entity.setMetadata(MetaFields.Sniffer.STATE, SnifferState.IDLING);
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            entity.setMetadata(MetaFields.Sniffer.STATE, SnifferState.FEELING_HAPPY);
        });
    }
}
