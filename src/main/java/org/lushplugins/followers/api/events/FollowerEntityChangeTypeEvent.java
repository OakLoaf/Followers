package org.lushplugins.followers.api.events;

import org.lushplugins.followers.entity.Follower;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FollowerEntityChangeTypeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Follower follower;
    private final String oldType;
    private final String newType;
    private boolean cancelled = false;

    public FollowerEntityChangeTypeEvent(@NotNull Follower follower, String oldType, String newType) {
        this.follower = follower;
        this.oldType = oldType;
        this.newType = newType;
    }

    public Follower getFollowerEntity() {
        return follower;
    }

    public String getOldType() {
        return oldType;
    }

    public String getNewType() {
        return newType;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
