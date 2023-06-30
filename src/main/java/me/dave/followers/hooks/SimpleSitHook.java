package me.dave.followers.hooks;

import me.dave.followers.Followers;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import me.dave.followers.entity.poses.FollowerPose;
import net.apcat.simplesit.events.PlayerSitEvent;
import net.apcat.simplesit.events.PlayerStopSittingEvent;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SimpleSitHook implements Listener {

    @EventHandler
    public void onPlayerSit(PlayerSitEvent event) {
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(event.getPlayer());
        followerUser.setPose(FollowerPose.SITTING);
        FollowerEntity followerEntity = followerUser.getFollowerEntity();
        if (followerEntity == null || !followerEntity.isAlive()) return;
        followerEntity.startParticles(Particle.CLOUD);
    }

    @EventHandler
    public void onPlayerExitSeat(PlayerStopSittingEvent event) {
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(event.getPlayer());
        followerUser.setPose(FollowerPose.DEFAULT);
        FollowerEntity followerEntity = followerUser.getFollowerEntity();
        if (followerEntity == null || !followerEntity.isAlive()) return;
        followerEntity.stopParticles();
    }
}
