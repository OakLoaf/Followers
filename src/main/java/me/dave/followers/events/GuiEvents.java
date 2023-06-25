package me.dave.followers.events;

import me.dave.followers.gui.AbstractGui;
import me.dave.followers.gui.BuilderGui;
import me.dave.followers.gui.MenuGui;
import me.dave.followers.gui.InventoryHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import me.dave.followers.Followers;

import java.util.UUID;

public class GuiEvents implements Listener {

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        AbstractGui gui = InventoryHandler.getGui(playerUUID);
        if (gui == null) return;
        gui.onClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        new BukkitRunnable() {
            public void run() {
                UUID playerUUID = event.getPlayer().getUniqueId();
                InventoryHandler.removeInventory(playerUUID);
            }
        }.runTaskLater(Followers.getInstance(), 1);
    }
}