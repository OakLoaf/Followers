package org.enchantedskies.esfollowers.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.datamanager.FollowerUser;

import java.util.HashMap;
import java.util.UUID;

public class FollowerUserEvents implements Listener {
    private final HashMap<UUID, UUID> playerFollowerMap;

    public FollowerUserEvents(HashMap<UUID, UUID> hashMap) {
        playerFollowerMap = hashMap;
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        if (entity.getType() != EntityType.ARMOR_STAND) return;
        UUID armorStandUUID = playerFollowerMap.get(player.getUniqueId());
        if (armorStandUUID == null) return;
        if (entity.getUniqueId() != armorStandUUID) return;
        player.sendMessage("Interacted with your follower.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ESFollowers.dataManager.loadFollowerUser(player.getUniqueId());
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
        followerUser.setUsername(player.getName());
        if (!followerUser.isFollowerEnabled()) {
            String follower = followerUser.getFollower();
            // spawnFollower
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
}
