package org.lushplugins.followers.listener;

import org.bukkit.event.Listener;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.api.events.FollowerEntitySpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.lushplugins.followers.entity.OwnedFollower;
import org.lushplugins.followers.utils.PlayerUtils;

public class FollowerEntityListener implements Listener {

    @EventHandler
    public void onFollowerSpawn(FollowerEntitySpawnEvent event) {
        if (!(event.getFollowerEntity() instanceof OwnedFollower ownedFollower)) {
            return;
        }

        if (!(ownedFollower.getOwner() instanceof Player player)) {
            return;
        }

        if (player.isInvisible() || PlayerUtils.isVanished(player) || !Followers.getInstance().getConfigManager().inEnabledWorld(player)) {
            event.setCancelled(true);
        }
    }
}
