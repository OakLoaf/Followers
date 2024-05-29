package org.lushplugins.followers.listener;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.FollowerEntitySpawnEvent;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.lushplugins.lushlib.listener.EventListener;

public class FollowerEntityListener implements EventListener {

    @EventHandler
    public void onFollowerSpawn(FollowerEntitySpawnEvent event) {
        Follower follower = event.getFollowerEntity();
        Player player = follower.getPlayer();
        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);

        if (player.isInvisible() || followerUser.isVanished()) {
            event.setCancelled(true);
        }
    }
}
