package org.lushplugins.followers.listener;

import org.lushplugins.followers.data.DataManager;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.entity.OwnedFollower;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.item.FollowerCreator;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.lushplugins.lushlib.listener.EventListener;

import java.util.UUID;

public class PlayerListener implements EventListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Followers.getInstance().getDataManager().loadFollowerUser(playerUUID).thenAccept(followerUser -> {
            followerUser.setUsername(player.getName());
            String followerName = followerUser.getFollowerType();
            if (followerUser.isFollowerEnabled() && player.hasPermission("followers." +  followerName.toLowerCase().replaceAll(" ", "_"))) {
                Bukkit.getScheduler().runTaskLater(Followers.getInstance(), followerUser::spawnFollower, 1);
            }
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DataManager dataManager = Followers.getInstance().getDataManager();
        FollowerUser followerUser = dataManager.getFollowerUser(player);
        OwnedFollower follower = followerUser.getFollower();
        if (follower != null) {
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), follower::despawn, 5);
        }

        dataManager.saveFollowerUser(followerUser);
        dataManager.unloadFollowerUser(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Follower follower = Followers.getInstance().getDataManager().getFollowerUser(player).getFollower();
        if (follower == null || !follower.isSpawned()) {
            return;
        }

        follower.setPose(FollowerPose.DEFAULT);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Follower follower = Followers.getInstance().getDataManager().getFollowerUser(player).getFollower();
        if (follower != null) {
            follower.despawn();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
        if (followerUser.isFollowerEnabled()) {
            followerUser.respawnFollower();

            if (followerUser.isRandomType()) {
                followerUser.randomiseFollowerType();
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
        if (Followers.getInstance().getConfigManager().getWorldBlacklist().contains(player.getWorld().getName())) {
            followerUser.setHidden(true);
        } else if (followerUser.isHidden()) {
            followerUser.setHidden(false);
        }

        Follower follower = followerUser.getFollower();
        if (follower != null) {
            follower.setWorld(player.getWorld());
        }
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent event) {
        try {
            EntityEquipment equipment = event.getEntity().getEquipment();
            EquipmentSlot hand = event.getHand();
            if (equipment == null || hand == null) {
                return;
            }

            if (equipment.getItem(hand).isSimilar(FollowerCreator.getOrLoadCreatorItem())) {
                event.setCancelled(true);
            }
        } catch (NoSuchMethodError ignored) {}
    }
}
