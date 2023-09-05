package me.dave.followers.api.events;

import me.dave.followers.entity.FollowerEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FollowerEntitySpawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final FollowerEntity followerEntity;
    private boolean cancelled = false;

    public FollowerEntitySpawnEvent(@NotNull FollowerEntity followerEntity) {
        this.followerEntity = followerEntity;
    }

    public FollowerEntity getFollowerEntity() {
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