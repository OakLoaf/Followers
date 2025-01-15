package org.lushplugins.followers.listener;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.lushplugins.followers.utils.menu.AnvilMenu;

// TODO: Move to PacketListener where possible
public class AnvilMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        AnvilMenu menu = AnvilMenu.getMenu(player.getUniqueId());
        if (menu == null) {
            return;
        }

        event.setCancelled(true);

        if (menu.isInputValid()) {
            player.closeInventory();
            menu.complete();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity player = event.getWhoClicked();
        AnvilMenu menu = AnvilMenu.getMenu(player.getUniqueId());
        if (menu == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity player = event.getPlayer();
        AnvilMenu menu = AnvilMenu.getMenu(player.getUniqueId());
        if (menu == null) {
            return;
        }

        AnvilMenu.removeMenu(player.getUniqueId());
        player.closeInventory();
        menu.cancel();
    }
}
