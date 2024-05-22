package org.lushplugins.followers.listener;

import org.lushplugins.followers.entity.poses.FollowerPose;
import org.lushplugins.followers.item.FollowerCreator;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.FollowerEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class FollowerUserListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Followers.dataManager.loadFollowerUser(playerUUID).thenAccept(followerUser -> {
            followerUser.setUsername(player.getName());
            String followerName = followerUser.getFollowerType();
            if (followerUser.isFollowerEnabled() && player.hasPermission("followers." +  followerName.toLowerCase().replaceAll(" ", "_"))) {
                Bukkit.getScheduler().runTaskLater(Followers.getInstance(), followerUser::spawnFollowerEntity, 1);
            }
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), followerUser::removeFollowerEntity, 5);
        Followers.dataManager.saveFollowerUser(followerUser);
        Followers.dataManager.unloadFollowerUser(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        FollowerEntity followerEntity = Followers.dataManager.getFollowerUser(player).getFollowerEntity();
        if (followerEntity == null || !followerEntity.isAlive()) {
            return;
        }

        followerEntity.setPose(FollowerPose.DEFAULT);
    }

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity.getPersistentDataContainer().has(Followers.getInstance().getFollowerKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        FollowerEntity followerEntity = Followers.dataManager.getFollowerUser(player).getFollowerEntity();
        if (followerEntity != null) {
            followerEntity.kill();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
        if (followerUser.isFollowerEnabled()) {
            followerUser.respawnFollowerEntity();
        }

        if (followerUser.isRandomType()) {
            followerUser.randomizeFollowerType();
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