package org.lushplugins.followers.entity.tasks;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.bukkit.entity.Player;
import org.lushplugins.followers.entity.OwnedFollower;
import org.lushplugins.followers.utils.PlayerUtils;

public class VisibilityTask extends FollowerTask {

    public VisibilityTask(String id) {
        super(id);
    }

    @Override
    public void tick(Follower follower) {
        if (!(follower instanceof OwnedFollower ownedFollower)) {
            return;
        }

        if (!(ownedFollower.getOwner() instanceof Player player)) {
            return;
        }

        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
        boolean hidden = player.isInvisible() || PlayerUtils.isVanished(player);
        if (followerUser.isHidden() != hidden) {
            followerUser.setHidden(hidden);
        }
    }

    @Override
    public int getPeriod() {
        return 20;
    }
}
