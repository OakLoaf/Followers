package org.enchantedskies.esfollowers.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class FollowerGUIEvents implements Listener {
    private final HashSet<UUID> playerSet;
    private final HashMap<UUID, UUID> playerFollowerMap;

    public FollowerGUIEvents(HashSet<UUID> playerSet, HashMap<UUID, UUID> playerFollowerMap) {
        this.playerSet = playerSet;
        this.playerFollowerMap = playerFollowerMap;
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        UUID playerUUID = event.getWhoClicked().getUniqueId();
        if (playerSet.contains(playerUUID)) event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        Player player = (Player) event.getWhoClicked();
        String followerName = clickedItem.getItemMeta().getDisplayName();
        if (playerFollowerMap.containsKey(player.getUniqueId())) {
            playerFollowerMap.get(player.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (!playerSet.contains(playerUUID)) return;
        playerSet.remove(playerUUID);
    }
}
