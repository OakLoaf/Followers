package me.dave.followers.entity.tasks;

import me.dave.followers.entity.FollowerEntity;

public abstract class AbstractTask {
    protected final FollowerEntity followerEntity;
    private final int startTick;
    private boolean cancelled = false;

    public AbstractTask(FollowerEntity followerEntity) {
        this.followerEntity = followerEntity;
        this.startTick = followerEntity.getTicksAlive() + getDelay();
    }

    public abstract void tick();

    public abstract FollowerTaskType getType();

    public abstract int getDelay();

    public abstract int getPeriod();

    public int getStartTick() {
        return startTick;
    }

    public void cancel() {
        cancelled = true;
        followerEntity.stopTask(getType());
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
