package org.lushplugins.followers.hooks;

import com.earth2me.essentials.Essentials;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;
import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
        FollowerEntity followerEntity = followerUser.getFollowerEntity();
        if (followerEntity == null || !followerEntity.isAlive()) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), (task) -> {
            if (iUser.isAfk()) {
                followerUser.setAfk(true);
                followerEntity.setPose(FollowerPose.SITTING);
            } else {
                followerUser.setAfk(false);
                if (!followerUser.isPosing()) {
                    followerUser.setPose(FollowerPose.DEFAULT);
                }
            }
        }, 1);
    }

    public boolean isVanished(UUID uuid) {
        IUser user = essentials.getUser(uuid);
        if (user == null) {
            return false;
        } else {
            return user.isVanished();
        }
    }
}