package org.lushplugins.followers.entity.tasks;

import org.bukkit.event.EventHandler;
import org.lushplugins.followers.api.events.FollowerTickEvent;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.lushlib.listener.EventListener;

public abstract class FollowerTask implements EventListener {
    private final String id;
    private boolean cancelled = false;

    public FollowerTask(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @EventHandler
    public void onFollowerTick(FollowerTickEvent event) {
        Follower follower = event.getFollower();
        if (follower.getTask(id) != null) {
            tick(event.getFollower());
        }
    }

    public abstract void tick(Follower follower);

    public abstract int getPeriod();

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel(Follower follower) {
        cancelled = true;
        follower.stopTask(getId());
    }
}
