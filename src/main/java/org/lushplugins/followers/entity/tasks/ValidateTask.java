package org.lushplugins.followers.entity.tasks;

import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.Follower;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.followers.entity.OwnedFollower;

public class ValidateTask extends FollowerTask {

    public ValidateTask(String id) {
        super(id);
    }

    @Override
    public void tick(Follower follower) {
        WrapperLivingEntity entity = follower.getEntity();
        if (entity == null) {
            cancelFor(follower);
            return;
        }

        if (!(follower instanceof OwnedFollower ownedFollower)) {
            return;
        }

        if (!(ownedFollower.getOwner() instanceof Player player)) {
            return;
        }

        if (!entity.isSpawned()) {
            follower.despawn();
            cancelFor(follower);
            return;
        }

        if (!player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), follower::despawn, 5);
        }
    }

    @Override
    public int getPeriod() {
        return 1;
    }
}
