package me.dave.followers.entity.tasks;

import me.dave.followers.entity.FollowerEntity;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class FollowerEntityTask extends BukkitRunnable {
    protected final FollowerEntity followerEntity;

    public FollowerEntityTask(FollowerEntity followerEntity) {
        this.followerEntity = followerEntity;
    }
}
