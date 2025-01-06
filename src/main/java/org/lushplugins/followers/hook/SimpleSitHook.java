package org.lushplugins.followers.hook;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.entity.poses.FollowerPose;
import net.apcat.simplesit.events.PlayerSitEvent;
import net.apcat.simplesit.events.PlayerStopSittingEvent;
import org.bukkit.event.EventHandler;
import org.lushplugins.followers.entity.tasks.TaskId;
import org.lushplugins.lushlib.hook.Hook;

public class SimpleSitHook extends Hook implements Listener {

    public SimpleSitHook() {
        super("SimpleSit");
    }

    @Override
    protected void onEnable() {
        Followers.getInstance().registerListeners();
    }

    @Override
    protected void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerSit(PlayerSitEvent event) {
        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(event.getPlayer());
        followerUser.setPose(FollowerPose.SITTING);
        Follower follower = followerUser.getFollower();
        if (follower == null || !follower.isSpawned()) {
            return;
        }

        follower.addTask(TaskId.PARTICLE_CLOUD);
    }

    @EventHandler
    public void onPlayerExitSeat(PlayerStopSittingEvent event) {
        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(event.getPlayer());
        followerUser.setPose(FollowerPose.DEFAULT);
        Follower follower = followerUser.getFollower();
        if (follower == null || !follower.isSpawned()) {
            return;
        }

        follower.removeTask(TaskId.PARTICLE_CLOUD);
    }
}
