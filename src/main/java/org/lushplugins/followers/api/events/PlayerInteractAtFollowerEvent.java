package org.lushplugins.followers.api.events;

import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.followers.entity.Follower;

public class PlayerInteractAtFollowerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Follower follower;
    private final InteractionHand hand;

    public PlayerInteractAtFollowerEvent(@NotNull final Player player, @NotNull final Follower follower) {
        this(player, follower, InteractionHand.MAIN_HAND);
    }

    public PlayerInteractAtFollowerEvent(@NotNull final Player player, @NotNull final Follower follower, @NotNull final InteractionHand hand) {
        this.player = player;
        this.follower = follower;
        this.hand = hand;
    }

    public Player getPlayer() {
        return player;
    }

    public Follower getFollower() {
        return follower;
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
