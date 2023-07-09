package me.dave.followers.gui.abstracts;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class AbstractGui {
    public abstract void recalculateContents();

    public abstract void openInventory();

    public abstract void onClick(InventoryClickEvent event);

    public abstract Inventory getInventory();
}
