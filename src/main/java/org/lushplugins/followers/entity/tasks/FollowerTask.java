package org.lushplugins.followers.entity.tasks;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;

public abstract class FollowerTask {
    private final int startTick = Followers.getInstance().getCurrentTick() + getDelay();
    private boolean cancelled = false;

    public abstract void tick(FollowerEntity follower);

    public abstract String getIdentifier();

    public int getDelay() {
        return 0;
    }

    public abstract int getPeriod();

    public int getStartTick() {
        return startTick;
    }

    public void cancel(FollowerEntity follower) {
        cancelled = true;
        follower.stopTask(getIdentifier());
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
