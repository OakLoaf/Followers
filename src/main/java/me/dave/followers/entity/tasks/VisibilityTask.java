package me.dave.followers.entity.tasks;

import me.dave.followers.entity.FollowerEntity;
import org.bukkit.entity.Player;

public class VisibilityTask extends AbstractEntityTask {
    private final Player player;

    public VisibilityTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
    }

    @Override
    public void run() {
        if (followerEntity.isPlayerInvisible() != player.isInvisible()) {
            followerEntity.setVisible(!player.isInvisible());
            followerEntity.setPlayerInvisible(player.isInvisible());
        }
    }
}
