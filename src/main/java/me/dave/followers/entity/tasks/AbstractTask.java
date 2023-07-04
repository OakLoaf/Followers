package me.dave.followers.entity.tasks;

import me.dave.followers.entity.FollowerEntity;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class AbstractTask extends BukkitRunnable {
    protected final FollowerEntity followerEntity;

    public AbstractTask(FollowerEntity followerEntity) {
        this.followerEntity = followerEntity;
    }

    public boolean checkCancel() {
        boolean shouldCancel = (followerEntity == null || !followerEntity.isAlive());
        if (shouldCancel) cancel();
        return shouldCancel;
    }

    public abstract FollowerTaskType getType();
}
