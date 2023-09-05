package me.dave.followers.events;

import me.dave.followers.Followers;
import me.dave.followers.api.events.FollowerEntitySpawnEvent;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FollowerEntityEvents implements Listener {

    @EventHandler
    public void onFollowerSpawn(FollowerEntitySpawnEvent event) {
        FollowerEntity followerEntity = event.getFollowerEntity();
        Player player = followerEntity.getPlayer();
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);

        if (player.isInvisible() || followerUser.isVanished()) {
            event.setCancelled(true);
        }
    }
}
