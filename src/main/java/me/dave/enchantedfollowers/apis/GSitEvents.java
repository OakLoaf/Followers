package me.dave.enchantedfollowers.apis;

import dev.geco.gsit.api.event.*;
import dev.geco.gsit.objects.GEmote;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.dave.enchantedfollowers.Followers;
import me.dave.enchantedfollowers.FollowerEntity;

public class GSitEvents implements Listener {

    @EventHandler
    public void onPlayerSit(PlayerSitEvent event) {
        Player player = event.getPlayer();
        FollowerEntity follower = Followers.dataManager.getPlayerFollowerMap().get(player.getUniqueId());
        if (follower == null) return;
        follower.setPose("sitting");
    }

    @EventHandler
    public void onPlayerGetUpFromSeat(PlayerGetUpSitEvent event) {
        Player player = event.getPlayer();
        FollowerEntity follower = Followers.dataManager.getPlayerFollowerMap().get(player.getUniqueId());
        if (follower == null) return;
        follower.setPose("default");
    }

    @EventHandler
    public void onPlayerEmote(EntityEmoteEvent e) {
        if (e.getEntity() instanceof Player player) {
            FollowerEntity follower = Followers.dataManager.getPlayerFollowerMap().get(player.getUniqueId());
            if (follower == null) return;
            e.getEmote().start(follower.getFollowerAS());
        }
    }

    @EventHandler
    public void onPlayerUnEmote(EntityStopEmoteEvent e) {
        if (e.getEntity() instanceof Player player) {
            FollowerEntity follower = Followers.dataManager.getPlayerFollowerMap().get(player.getUniqueId());
            if (follower == null) return;
            e.getEmote().stop(follower.getFollowerAS());
        }
    }
}
