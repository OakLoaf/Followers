package org.enchantedskies.esfollowers.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashSet;
import java.util.UUID;

public class FollowerGUIEvents implements Listener {
    private final HashSet<UUID> playerSet;

    public FollowerGUIEvents(HashSet<UUID> playerSet) {
        this.playerSet = playerSet;
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        UUID playerUUID = event.getWhoClicked().getUniqueId();
        if (playerSet.contains(playerUUID)) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (!playerSet.contains(playerUUID)) return;
        playerSet.remove(playerUUID);
    }
}
