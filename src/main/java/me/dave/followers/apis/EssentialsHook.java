package me.dave.followers.apis;

import com.earth2me.essentials.Essentials;
import dev.geco.gsit.api.GSitAPI;
import me.dave.followers.entity.pose.FollowerPose;
import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;

import java.util.UUID;

public class EssentialsHook implements Listener {
    private final Essentials essentials;

    public EssentialsHook() {
        essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    @EventHandler
    public void onAFK(AfkStatusChangeEvent event) {
        IUser iUser = event.getAffected();
        Player player = iUser.getBase();
        FollowerEntity follower = Followers.dataManager.getPlayerFollowerMap().get(player.getUniqueId());
        if (follower == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (iUser.isAfk()) follower.setPose(FollowerPose.SITTING);
                else {
                    if (Followers.hasGSit() && GSitAPI.isSitting(player)) follower.setPose(FollowerPose.SITTING);
                    else follower.setPose(FollowerPose.DEFAULT);
                }
            }
        }.runTaskLater(Followers.getInstance(), 1);
    }

    public boolean isVanished(UUID uuid) {
        IUser user = essentials.getUser(uuid);
        if (user == null) return false;
        return user.isVanished();
    }
}
