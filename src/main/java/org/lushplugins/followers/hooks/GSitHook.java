package org.lushplugins.followers.hooks;

import dev.geco.gsit.api.event.*;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.lushplugins.lushlib.hook.Hook;
import org.lushplugins.lushlib.listener.EventListener;

public class GSitHook extends Hook implements EventListener {

    public GSitHook() {
        super("GSit");
    }

    @Override
    protected void onEnable() {
        this.registerListeners();
    }

    @Override
    protected void onDisable() {
        this.unregisterListeners();
    }

    @EventHandler
    public void onPlayerSit(EntitySitEvent event) {
        if (event.getEntity() instanceof Player player) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.setPose(FollowerPose.SITTING);
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (followerEntity == null || !followerEntity.isAlive()) {
                return;
            }

            followerEntity.startParticles(Particle.CLOUD);
        }
    }

    @EventHandler
    public void onPlayerGetUpFromSeat(EntityGetUpSitEvent event) {
        if (event.getEntity() instanceof Player player) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.setPose(FollowerPose.DEFAULT);
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (followerEntity == null || !followerEntity.isAlive()) {
                return;
            }

            followerEntity.stopTask("particle");
        }
    }

    @EventHandler
    public void onPlayerPose(PlayerPoseEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = Followers.getInstance().getDataManager().getFollowerUser(player).getFollowerEntity();
        if (followerEntity == null || !followerEntity.isAlive()) {
            return;
        }

        Pose pose = event.getPoseSeat().getPose();
        if (pose == Pose.SPIN_ATTACK) {
            followerEntity.setPose(FollowerPose.SPINNING);
            followerEntity.startParticles(Particle.CLOUD);
        }
    }

    @EventHandler
    public void onPlayerEndPose(PlayerGetUpPoseEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = Followers.getInstance().getDataManager().getFollowerUser(player).getFollowerEntity();
        if (followerEntity == null || !followerEntity.isAlive()) {
            return;
        }
        
        followerEntity.setPose(FollowerPose.DEFAULT);
        followerEntity.stopTask("particle");
    }
}
