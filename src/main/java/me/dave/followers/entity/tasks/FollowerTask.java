package me.dave.followers.entity.tasks;

import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;

public abstract class FollowerTask {
    protected final FollowerEntity followerEntity;
    private final int startTick;
    private boolean cancelled = false;

    public FollowerTask(FollowerEntity followerEntity) {
        this.followerEntity = followerEntity;
        this.startTick = Followers.getCurrentTick() + getDelay();
    }

    public abstract void tick();

    public abstract String getIdentifier();

    public abstract int getDelay();

    public abstract int getPeriod();

    public int getStartTick() {
        return startTick;
    }

    public void cancel() {
        cancelled = true;
        followerEntity.stopTask(getIdentifier());
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
