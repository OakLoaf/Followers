package me.dave.followers.events;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataType;
import me.dave.followers.Followers;
import me.dave.followers.FollowerEntity;
import me.dave.followers.datamanager.FollowerUser;

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
        CompletableFuture<FollowerUser> future = Followers.dataManager.loadFollowerUser(playerUUID);
        future.thenAccept(followerUser -> {
            followerUser.setUsername(player.getName());
            String followerName = followerUser.getFollower();
            if (followerUser.isFollowerEnabled() && player.hasPermission("followers." + followerName)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new FollowerEntity(player, followerName);
                    }
                }.runTask(Followers.getInstance());
            }
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = playerFollowerMap.get(player.getUniqueId());
        if (followerEntity != null) followerEntity.kill();
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
        Followers.dataManager.saveFollowerUser(followerUser);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = playerFollowerMap.get(player.getUniqueId());
        if (followerEntity == null) return;
        followerEntity.setPose("default");
    }

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }
}
