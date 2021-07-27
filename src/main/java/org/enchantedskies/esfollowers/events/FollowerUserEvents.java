package org.enchantedskies.esfollowers.events;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataType;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerEntity;
import org.enchantedskies.esfollowers.datamanager.FollowerUser;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FollowerUserEvents implements Listener {
    private final NamespacedKey followerKey = new NamespacedKey(ESFollowers.getInstance(), "ESFollower");
    private final HashMap<UUID, FollowerEntity> playerFollowerMap = ESFollowers.dataManager.getPlayerFollowerMap();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        CompletableFuture<FollowerUser> future = ESFollowers.dataManager.loadFollowerUser(playerUUID);
        future.thenAccept(followerUser -> {
            followerUser.setUsername(player.getName());
            String followerName = followerUser.getFollower();
            if (followerUser.isFollowerEnabled() && player.hasPermission("followers." + followerName)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new FollowerEntity(player, followerName);
                    }
                }.runTask(ESFollowers.getInstance());
            }
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = playerFollowerMap.get(player.getUniqueId());
        if (followerEntity != null) followerEntity.kill();
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
        ESFollowers.dataManager.saveFollowerUser(followerUser);
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
