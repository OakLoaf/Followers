package me.dave.followers.entity.tasks;

import me.dave.followers.Followers;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ValidateTask extends FollowerEntityTask {
    private final Player player;

    public ValidateTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
    }

    @Override
    public void run() {
        if (followerEntity == null || followerEntity.isDying()) {
            cancel();
            return;
        }
        if (followerEntity.getBodyArmorStand() == null || !followerEntity.getBodyArmorStand().isValid() || !followerEntity.isAlive) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            if (followerUser != null) followerUser.respawnFollowerEntity();
            else followerEntity.kill();
            cancel();
            return;
        }
        if (!player.isOnline()) Bukkit.getScheduler().runTaskLater(Followers.getInstance(), followerEntity::kill, 5);
    }
}
