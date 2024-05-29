package org.lushplugins.followers.hooks;

import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import dev.geco.gsit.api.event.*;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.Followers;
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
            Follower follower = followerUser.getFollowerEntity();
            if (follower == null || !follower.isAlive()) {
                return;
            }

            follower.startParticles(ParticleTypes.CLOUD);
        }
    }

    @EventHandler
    public void onPlayerGetUpFromSeat(EntityGetUpSitEvent event) {
        if (event.getEntity() instanceof Player player) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.setPose(FollowerPose.DEFAULT);
            Follower follower = followerUser.getFollowerEntity();
            if (follower == null || !follower.isAlive()) {
                return;
            }

            follower.stopTask("particle");
        }
    }

    @EventHandler
    public void onPlayerPose(PlayerPoseEvent event) {
        Player player = event.getPlayer();
        Follower follower = Followers.getInstance().getDataManager().getFollowerUser(player).getFollowerEntity();
        if (follower == null || !follower.isAlive()) {
            return;
        }

        Pose pose = event.getPoseSeat().getPose();
        if (pose == Pose.SPIN_ATTACK) {
            follower.setPose(FollowerPose.SPINNING);
            follower.startParticles(ParticleTypes.CLOUD);
        }
    }

    @EventHandler
    public void onPlayerEndPose(PlayerGetUpPoseEvent event) {
        Player player = event.getPlayer();
        Follower follower = Followers.getInstance().getDataManager().getFollowerUser(player).getFollowerEntity();
        if (follower == null || !follower.isAlive()) {
            return;
        }
        
        follower.setPose(FollowerPose.DEFAULT);
        follower.stopTask("particle");
    }
}
