package me.dave.followers.events;

import me.dave.followers.entity.pose.FollowerPose;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataType;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;
import me.dave.followers.data.FollowerUser;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FollowerUserEvents implements Listener {
    private final NamespacedKey followerKey = new NamespacedKey(Followers.getInstance(), "Follower");
    private final HashMap<UUID, FollowerEntity> playerFollowerMap = Followers.dataManager.getPlayerFollowerMap();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Followers.dataManager.loadFollowerUser(playerUUID).thenAccept(followerUser -> {
            followerUser.setUsername(player.getName());
            String followerName = followerUser.getFollower();
            if (followerUser.isFollowerEnabled() && player.hasPermission("followers." +  followerName.toLowerCase().replaceAll(" ", "_"))) {
                Bukkit.getScheduler().runTask(Followers.getInstance(), () -> new FollowerEntity(player, followerName));
            }
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = playerFollowerMap.get(player.getUniqueId());
        if (followerEntity != null) Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> followerEntity.kill(false), 5);
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
        Followers.dataManager.saveFollowerUser(followerUser);
        Followers.dataManager.unloadFollowerUser(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = playerFollowerMap.get(player.getUniqueId());
        if (followerEntity == null) return;
        followerEntity.setPose(FollowerPose.DEFAULT);
    }

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }
}
