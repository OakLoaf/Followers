package me.dave.followers.events.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;


// TODO: implement inside FollowerEntity class
public class PlayerVisiblityChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final boolean oldVisiblity;
    private final boolean newVisibility;

    public PlayerVisiblityChangeEvent(@NotNull Player player, boolean oldVisibility, boolean newVisibility) {
        super(player);
        this.oldVisiblity = oldVisibility;
        this.newVisibility = newVisibility;
    }

    public boolean getOldVisibility() {
        return oldVisiblity;
    }

    public boolean getNewVisibility() {
        return newVisibility;
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
