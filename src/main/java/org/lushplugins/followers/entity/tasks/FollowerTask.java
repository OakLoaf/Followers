package org.lushplugins.followers.entity.tasks;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;

public abstract class FollowerTask {
    private final String id;
    private final int startTick = Followers.getInstance().getCurrentTick() + getDelay();
    private boolean cancelled = false;

    public FollowerTask(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract void tick(FollowerEntity follower);

    public int getDelay() {
        return 0;
    }

    public abstract int getPeriod();

    public int getStartTick() {
        return startTick;
    }

    public void cancel(FollowerEntity follower) {
        cancelled = true;
        follower.stopTask(getId());
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
