package me.dave.followers.entity.tasks;

import me.dave.followers.entity.FollowerEntity;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class AbstractEntityTask extends BukkitRunnable {
    protected final FollowerEntity followerEntity;

    public AbstractEntityTask(FollowerEntity followerEntity) {
        this.followerEntity = followerEntity;
    }
}
