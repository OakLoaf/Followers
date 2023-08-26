package me.dave.followers.events;

import me.dave.followers.gui.abstracts.AbstractGui;
import me.dave.followers.gui.InventoryHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class GuiEvents implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();

        AbstractGui gui = InventoryHandler.getGui(player.getUniqueId());
        if (gui == null || !event.getInventory().equals(gui.getInventory())) {
            return;
        }

        gui.onOpen(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        AbstractGui gui = InventoryHandler.getGui(player.getUniqueId());
        if (gui == null || !event.getInventory().equals(gui.getInventory())) {
            return;
        }

        gui.onClose(event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        AbstractGui gui = InventoryHandler.getGui(player.getUniqueId());
        if (gui == null) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !player.getOpenInventory().getTopInventory().equals(gui.getInventory())) {
            return;
        }

        gui.onClick(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        AbstractGui gui = InventoryHandler.getGui(playerUUID);
        if (gui == null || !player.getOpenInventory().getTopInventory().equals(gui.getInventory())) {
            return;
        }

        gui.onDrag(event);
    }
}