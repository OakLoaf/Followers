package org.lushplugins.followers.entity.tasks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.FollowerTickEvent;
import org.lushplugins.followers.entity.Follower;

public abstract class FollowerTask implements Listener {
    private final String id;

    public FollowerTask(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @EventHandler
    public void onFollowerTick(FollowerTickEvent event) {
        Follower follower = event.getFollower();
        if (Followers.getInstance().getCurrentTick() % this.getPeriod() == 0 && follower.hasTask(id)) {
            tick(event.getFollower());
        }
    }

    public abstract void tick(Follower follower);

    public abstract int getPeriod();

    public void cancelFor(Follower follower) {
        follower.removeTask(this.getId());
    }
}
