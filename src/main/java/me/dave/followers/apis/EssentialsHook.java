package me.dave.followers.apis;

import com.earth2me.essentials.Essentials;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.poses.FollowerPose;
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
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
        FollowerEntity followerEntity = followerUser.getFollowerEntity();
        if (followerEntity == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (iUser.isAfk()) {
                    followerUser.setAfk(true);
                    followerEntity.setPose(FollowerPose.SITTING);
                } else {
                    followerUser.setAfk(false);
                    if (!followerUser.isPosing()) followerUser.setPose(FollowerPose.DEFAULT);
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
