package org.lushplugins.followers.api;

import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.poses.FollowerPoseRegistry;
import org.lushplugins.followers.entity.tasks.FollowerTaskRegistry;

@SuppressWarnings("unused")
public class FollowersAPI {

    /**
     * Get the task registry - this can be used to register new follower tasks
     * @return the task registry
     */
    public static FollowerTaskRegistry getTaskRegistry() {
        return Followers.getInstance().getFollowerManager().getTaskRegistry();
    }

    /**
     * Get the pose registry - this can be used to register new follower poses
     * @return the task registry
     */
    public static FollowerPoseRegistry getPoseRegistry() {
        return Followers.getInstance().getFollowerManager().getPoseRegistry();
    }

    /**
     * Get information about a follower user
     * @param player the player
     * @return the user data of the player
     */
    public static FollowerUser getFollowerUser(Player player) {
        return Followers.getInstance().getDataManager().getFollowerUser(player);
    }
}
