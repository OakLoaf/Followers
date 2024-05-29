package org.lushplugins.followers.api.events;

import org.lushplugins.followers.entity.FollowerEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FollowerTickEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final FollowerEntity followerEntity;
    private boolean cancelled = false;

    public FollowerTickEvent(@NotNull FollowerEntity followerEntity) {
        this.followerEntity = followerEntity;
    }

    public FollowerEntity getFollower() {
        return followerEntity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
