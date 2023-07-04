package me.dave.followers.entity.tasks;

import me.dave.followers.Followers;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import org.bukkit.entity.Player;

public class VisibilityTask extends AbstractTask {
    private final Player player;

    public VisibilityTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
    }

    @Override
    public FollowerTaskType getType() {
        return FollowerTaskType.VISIBILITY;
    }

    @Override
    public void run() {
        if (checkCancel()) return;

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(followerEntity.getPlayer());
        boolean visible = !player.isInvisible() && !followerUser.isVanished();

        if (visible != followerEntity.isVisible()) followerEntity.setVisible(visible);
    }
}
