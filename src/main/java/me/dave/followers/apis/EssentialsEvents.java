package me.dave.followers.apis;

import dev.geco.gsit.api.GSitAPI;
import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import me.dave.followers.Followers;
import me.dave.followers.FollowerEntity;

public class EssentialsEvents implements Listener {
    private final Followers plugin = Followers.getInstance();

    @EventHandler
    public void onAFK(AfkStatusChangeEvent event) {
        IUser iUser = event.getAffected();
        Player player = iUser.getBase();
        FollowerEntity follower = Followers.dataManager.getPlayerFollowerMap().get(player.getUniqueId());
        if (follower == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (iUser.isAfk()) follower.setPose("sitting");
                else {
                    if (Followers.hasGSit() && GSitAPI.isSitting(player)) follower.setPose("sitting");
                    else follower.setPose("default");
                }
            }
        }.runTaskLater(plugin, 1);
    }
}
