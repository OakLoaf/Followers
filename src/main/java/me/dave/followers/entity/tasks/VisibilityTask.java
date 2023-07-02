package me.dave.followers.entity.tasks;

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
        if (followerEntity.isPlayerInvisible() != player.isInvisible()) {
            followerEntity.setVisible(!player.isInvisible());
            followerEntity.setPlayerInvisible(player.isInvisible());
        }
    }
}
