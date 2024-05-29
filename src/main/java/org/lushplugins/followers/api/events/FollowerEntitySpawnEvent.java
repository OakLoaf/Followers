package org.lushplugins.followers.api.events;

import org.lushplugins.followers.entity.Follower;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FollowerEntitySpawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Follower follower;
    private boolean cancelled = false;

    public FollowerEntitySpawnEvent(@NotNull Follower follower) {
        this.follower = follower;
    }

    public Follower getFollowerEntity() {
        return follower;
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