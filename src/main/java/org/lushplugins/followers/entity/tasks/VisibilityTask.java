package org.lushplugins.followers.entity.tasks;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.FollowerEntity;
import org.bukkit.entity.Player;

public class VisibilityTask extends FollowerTask {
    private final Player player;

    public VisibilityTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
    }

    @Override
    public void tick() {
        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);

        boolean hidden = player.isInvisible() || followerUser.isVanished();
        followerUser.setHidden(hidden);
    }

    @Override
    public String getIdentifier() {
        return TaskId.VISIBILITY;
    }

    @Override
    public int getDelay() {
        return 5;
    }

    @Override
    public int getPeriod() {
        return 20;
    }
}
