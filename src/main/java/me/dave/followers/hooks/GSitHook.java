package me.dave.followers.hooks;

import dev.geco.gsit.api.event.*;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.poses.FollowerPose;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;

public class GSitHook implements Listener {

    @EventHandler
    public void onPlayerSit(EntitySitEvent event) {
        if (event.getEntity() instanceof Player player) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            followerUser.setPose(FollowerPose.SITTING);
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (followerEntity == null || !followerEntity.isAlive()) return;
            followerEntity.startParticles(Particle.CLOUD);
        }
    }

    @EventHandler
    public void onPlayerGetUpFromSeat(EntityGetUpSitEvent event) {
        if (event.getEntity() instanceof Player player) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            followerUser.setPose(FollowerPose.DEFAULT);
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (followerEntity == null || !followerEntity.isAlive()) return;
            followerEntity.stopParticles();
        }
    }

    @EventHandler
    public void onPlayerPose(PlayerPoseEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = Followers.dataManager.getFollowerUser(player).getFollowerEntity();
        if (followerEntity == null) return;
        Pose pose = event.getPoseSeat().getPose();
        if (pose == Pose.SPIN_ATTACK) {
            followerEntity.setPose(FollowerPose.SPINNING);
            followerEntity.startParticles(Particle.CLOUD);
        }
    }

    @EventHandler
    public void onPlayerUnpose(PlayerGetUpPoseEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = Followers.dataManager.getFollowerUser(player).getFollowerEntity();
        if (followerEntity == null) return;
        followerEntity.setPose(FollowerPose.DEFAULT);
        followerEntity.stopParticles();
    }
}
