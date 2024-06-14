package org.lushplugins.followers.entity.poses.entities;

import com.github.retrooper.packetevents.protocol.entity.armadillo.ArmadilloState;
import me.tofaa.entitylib.meta.mobs.passive.ArmadilloMeta;
import org.bukkit.Bukkit;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.entity.poses.PoseSet;

public class ArmadilloPoses extends PoseSet {

    public ArmadilloPoses() {
        addPose(FollowerPose.DEFAULT, (entity) -> {
            ArmadilloMeta entityMeta = (ArmadilloMeta) entity.getEntityMeta();

            switch (entityMeta.getState()) {
                case SCARED -> {
                    entityMeta.setState(ArmadilloState.UNROLLING);
                    entity.refresh();

                    Bukkit.getScheduler().runTaskLaterAsynchronously(Followers.getInstance(), () -> {
                        entityMeta.setState(ArmadilloState.IDLE);
                        entity.refresh();
                    }, 30);
                }
                case IDLE -> {}
                default -> Bukkit.getScheduler().runTaskLaterAsynchronously(Followers.getInstance(), () -> {
                    entityMeta.setState(ArmadilloState.IDLE);
                    entity.refresh();
                }, 30);
            }
        });

        addPose(FollowerPose.SITTING, (entity) -> {
            ArmadilloMeta entityMeta = (ArmadilloMeta) entity.getEntityMeta();

            switch (entityMeta.getState()) {
                case IDLE -> {
                    entityMeta.setState(ArmadilloState.ROLLING);
                    entity.refresh();

                    Bukkit.getScheduler().runTaskLaterAsynchronously(Followers.getInstance(), () -> {
                        entityMeta.setState(ArmadilloState.SCARED);
                        entity.refresh();
                    }, 30);
                }
                case SCARED -> {}
                default -> Bukkit.getScheduler().runTaskLaterAsynchronously(Followers.getInstance(), () -> {
                    entityMeta.setState(ArmadilloState.SCARED);
                    entity.refresh();
                }, 30);
            }
        });
    }
}
