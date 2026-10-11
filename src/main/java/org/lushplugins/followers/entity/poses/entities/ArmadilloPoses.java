package org.lushplugins.followers.entity.poses.entities;

import com.github.retrooper.packetevents.protocol.entity.armadillo.ArmadilloState;
import me.tofaa.entitylib.meta.MetaFields;
import org.bukkit.Bukkit;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class ArmadilloPoses extends PoseSet {

    public ArmadilloPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            switch (entity.getMetadata().get(MetaFields.Armadillo.ARMADILLO_STATE)) {
                case SCARED -> {
                    entity.setMetadata(MetaFields.Armadillo.ARMADILLO_STATE, ArmadilloState.UNROLLING);

                    Bukkit.getScheduler().runTaskLaterAsynchronously(Followers.getInstance(), () -> {
                        entity.setMetadata(MetaFields.Armadillo.ARMADILLO_STATE, ArmadilloState.IDLE);
                    }, 30);
                }
                case IDLE -> {}
                default -> Bukkit.getScheduler().runTaskLaterAsynchronously(Followers.getInstance(), () -> {
                    entity.setMetadata(MetaFields.Armadillo.ARMADILLO_STATE, ArmadilloState.IDLE);
                }, 30);
            }
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            switch (entity.getMetadata().get(MetaFields.Armadillo.ARMADILLO_STATE)) {
                case IDLE -> {
                    entity.setMetadata(MetaFields.Armadillo.ARMADILLO_STATE, ArmadilloState.ROLLING);

                    Bukkit.getScheduler().runTaskLaterAsynchronously(Followers.getInstance(), () -> {
                        entity.setMetadata(MetaFields.Armadillo.ARMADILLO_STATE, ArmadilloState.SCARED);
                    }, 30);
                }
                case SCARED -> {}
                default -> Bukkit.getScheduler().runTaskLaterAsynchronously(Followers.getInstance(), () -> {
                    entity.setMetadata(MetaFields.Armadillo.ARMADILLO_STATE, ArmadilloState.SCARED);
                }, 30);
            }
        });
    }
}
