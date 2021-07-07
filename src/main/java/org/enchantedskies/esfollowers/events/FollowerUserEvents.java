package org.enchantedskies.esfollowers.events;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
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
        future.thenAcceptAsync(followerUser -> {
            followerUser.setUsername(player.getName());
            String followerName = followerUser.getFollower();
            if (followerUser.isFollowerEnabled() && player.hasPermission("followers." + followerName)) {
                new FollowerEntity(player, followerName);
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
        Chunk fromChunk = event.getFrom().getChunk();
        Player player = event.getPlayer();
        FollowerEntity followerEntity = ESFollowers.dataManager.getPlayerFollowerMap().get(player.getUniqueId());
        if (followerEntity == null) return;
        UUID followerASUUID = followerEntity.getArmorStand().getUniqueId();
        ArmorStand nameTagAS = followerEntity.getArmorStand();
        for (Entity entity : fromChunk.getEntities()) {
            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
                if (!fromChunk.isLoaded()) fromChunk.load();
                UUID entityUUID = entity.getUniqueId();
                if (entityUUID == followerASUUID || (nameTagAS != null && entityUUID == nameTagAS.getUniqueId())) entity.teleport(player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }
}
