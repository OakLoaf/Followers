package me.dave.followers.entity.tasks;

import me.dave.followers.entity.FollowerEntity;

public abstract class FollowerTask {
    public static final String MOVEMENT = "movement";
    public static final String PARTICLE = "particle";
    public static final String VALIDATE = "validate";
    public static final String VISIBILITY = "visibility";

    protected final FollowerEntity followerEntity;
    private final int startTick;
    private boolean cancelled = false;

    public FollowerTask(FollowerEntity followerEntity) {
        this.followerEntity = followerEntity;
        this.startTick = followerEntity.getTicksAlive() + getDelay();
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
