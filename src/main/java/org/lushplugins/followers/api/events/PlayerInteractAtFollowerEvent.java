package org.lushplugins.followers.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.followers.entity.Follower;

// TODO: Implement (or alternatively consider triggering PlayerInteractAtEntityEvent)
public class PlayerInteractAtFollowerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Follower follower;

    public PlayerInteractAtFollowerEvent(Player player, Follower follower) {
        this.player = player;
        this.follower = follower;
    }

    public Player getPlayer() {
        return player;
    }

    public Follower getFollowerEntity() {
        return follower;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
