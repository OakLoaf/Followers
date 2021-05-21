package org.enchantedskies.esfollowers.events;

import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerArmorStand;
import org.enchantedskies.esfollowers.datamanager.FollowerUser;

import java.util.HashMap;
import java.util.UUID;

public class FollowerUserEvents implements Listener {
    private final ESFollowers plugin = ESFollowers.getInstance();;
    private final NamespacedKey followerKey;
    private final HashMap<UUID, UUID> playerFollowerMap;

    public FollowerUserEvents(HashMap<UUID, UUID> playerFollowerMap, NamespacedKey followerKey) {
        this.playerFollowerMap = playerFollowerMap;
        this.followerKey = followerKey;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ESFollowers.dataManager.loadFollowerUser(player.getUniqueId());
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
        followerUser.setUsername(player.getName());
        String followerName = followerUser.getFollower();
        if (followerUser.isFollowerEnabled() && player.hasPermission("followers." + followerName)) {
            FollowerArmorStand followerArmorStand = new FollowerArmorStand(followerName, player, playerFollowerMap, followerKey);
            followerArmorStand.startMovement(0.4);
            playerFollowerMap.put(player.getUniqueId(), followerArmorStand.getArmorStand().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID followerUUID = playerFollowerMap.get(player.getUniqueId());
        if (followerUUID == null) return;
        Entity entity = Bukkit.getEntity(followerUUID);
        playerFollowerMap.remove(player.getUniqueId());
        if (entity == null) return;
        entity.remove();
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
        ESFollowers.dataManager.saveFollowerUser(followerUser);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Chunk fromChunk = event.getFrom().getChunk();
        UUID playerUUID = event.getPlayer().getUniqueId();
        HashBiMap<UUID, UUID> playerFollowerBiMap = HashBiMap.create(playerFollowerMap);
        for (Entity entity : fromChunk.getEntities()) {
            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
                if (!fromChunk.isLoaded()) fromChunk.load();
                UUID connectedPlayerUUID = playerFollowerBiMap.inverse().get(entity.getUniqueId());
                if (connectedPlayerUUID != playerUUID) continue;
                entity.teleport(event.getPlayer());
                return;
            }
        }
    }
}
