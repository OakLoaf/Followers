package org.enchantedskies.esfollowers.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerArmorStand;
import org.enchantedskies.esfollowers.datamanager.FollowerUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class FollowerGUIEvents implements Listener {
    private final ESFollowers plugin;
    private final HashSet<UUID> openInvPlayerSet;
    private final NamespacedKey followerKey;
    private final HashMap<UUID, UUID> playerFollowerMap;
    private final HashMap<String, ItemStack> followerSkullMap;

    public FollowerGUIEvents(ESFollowers instance, HashSet<UUID> playerSet, HashMap<UUID, UUID> playerFollowerMap, HashMap<String, ItemStack> followerSkullMap, NamespacedKey followerKey) {
        plugin = instance;
        this.openInvPlayerSet = playerSet;
        this.followerKey = followerKey;
        this.playerFollowerMap = playerFollowerMap;
        this.followerSkullMap = followerSkullMap;
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        if (!openInvPlayerSet.contains(playerUUID)) return;
        event.setCancelled(true);
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;
        if (clickedInv.getType() != InventoryType.CHEST) return;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        String followerName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        if (playerFollowerMap.containsKey(player.getUniqueId())) {
            UUID armorstandUUID = playerFollowerMap.get(player.getUniqueId());
            new FollowerArmorStand(plugin, followerName, (ArmorStand) Bukkit.getEntity(armorstandUUID), followerSkullMap);
            FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
            followerUser.setFollower(followerName);
            player.closeInventory();
            return;
        }
        FollowerArmorStand followerArmorStand = new FollowerArmorStand(plugin, followerName, player, followerSkullMap, playerFollowerMap, followerKey);
        followerArmorStand.startMovement(0.4);
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
        followerUser.setFollower(followerName);
        playerFollowerMap.put(player.getUniqueId(), followerArmorStand.getArmorStand().getUniqueId());
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Follower Spawned.");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (!openInvPlayerSet.contains(playerUUID)) return;
        openInvPlayerSet.remove(playerUUID);
    }
}
