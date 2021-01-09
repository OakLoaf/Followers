package org.enchantedskies.esfollowers.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerArmorStand;
import org.enchantedskies.esfollowers.datamanager.FollowerUser;

import java.util.HashMap;
import java.util.UUID;

public class FollowerUserEvents implements Listener {
    private final ESFollowers plugin;
    private final HashMap<String, ItemStack> followerSkullMap;
    private final HashMap<UUID, UUID> playerFollowerMap;

    public FollowerUserEvents(ESFollowers instance, HashMap<String, ItemStack> followerSkullMap, HashMap<UUID, UUID> playerFollowerMap) {
        plugin = instance;
        this.followerSkullMap = followerSkullMap;
        this.playerFollowerMap = playerFollowerMap;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ESFollowers.dataManager.loadFollowerUser(player.getUniqueId());
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
        followerUser.setUsername(player.getName());
        if (followerUser.isFollowerEnabled()) {
            String followerName = followerUser.getFollower();
            FollowerArmorStand followerArmorStand = new FollowerArmorStand(plugin, followerName, player, followerSkullMap, playerFollowerMap);
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
}
