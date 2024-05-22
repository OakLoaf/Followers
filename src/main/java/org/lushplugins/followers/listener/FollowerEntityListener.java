package org.lushplugins.followers.listener;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.FollowerEntitySpawnEvent;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.FollowerEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FollowerEntityListener implements Listener {

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
