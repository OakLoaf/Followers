package me.dave.followers.api.events;

import me.dave.followers.entity.FollowerEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FollowerEntityChangeTypeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final FollowerEntity followerEntity;
    private final String oldType;
    private final String newType;
    private boolean cancelled = false;

    public FollowerEntityChangeTypeEvent(@NotNull FollowerEntity followerEntity, String oldType, String newType) {
        this.followerEntity = followerEntity;
        this.oldType = oldType;
        this.newType = newType;
    }

    public FollowerEntity getFollowerEntity() {
        return followerEntity;
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
