package org.enchantedskies.esfollowers.apis;

import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerEntity;

public class EssentialsEvents implements Listener {
    private final ESFollowers plugin = ESFollowers.getInstance();

    @EventHandler
    public void onAFK(AfkStatusChangeEvent event) {
        IUser iUser = event.getAffected();
        Player player = iUser.getBase();
        FollowerEntity follower = ESFollowers.dataManager.getPlayerFollowerMap().get(player.getUniqueId());
        if (follower == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (iUser.isAfk()) follower.setPose("sitting");
                else {
                    if (ESFollowers.isGSitEnabled && ESFollowers.GAPI.isSitting(player)) follower.setPose("sitting");
                    else follower.setPose("default");
                }
            }
        }.runTaskLater(plugin, 1);
    }
}
