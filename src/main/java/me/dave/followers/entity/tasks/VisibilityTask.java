package me.dave.followers.entity.tasks;

import me.dave.followers.Followers;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import org.bukkit.entity.Player;

public class VisibilityTask extends FollowerTask {
    private final Player player;

    public VisibilityTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
    }

    @Override
    public void tick() {
        // TODO: Resolve task not respawning follower when removing invisibility
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);

        boolean hidden = player.isInvisible() || followerUser.isVanished();
        followerUser.setHidden(hidden);
    }

    @Override
    public String getIdentifier() {
        return "visibility";
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
