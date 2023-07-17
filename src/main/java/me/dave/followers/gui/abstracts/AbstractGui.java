package me.dave.followers.gui.abstracts;


import me.dave.followers.gui.InventoryHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public abstract class AbstractGui {
    protected final Inventory inventory;
    protected final Player player;
    private final HashMap<Integer, Boolean> slotLockMap = new HashMap<>();

    public AbstractGui(int size, String title, Player player) {
        inventory = Bukkit.createInventory(null, size, title);
        this.player = player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public void lockSlot(int slot) {
        lockSlot(slot, true);
    }

    public void unlockSlot(int slot) {
        lockSlot(slot, false);
    }

    public void lockSlot(int slot, boolean locked) {
        slotLockMap.put(slot, locked);
    }

    public void lockSlots(int... slots) {
        for (int slot : slots) {
            lockSlot(slot, true);
        }
    }

    public void unlockSlots(int... slots) {
        for (int slot : slots) {
            lockSlot(slot, false);
        }
    }

    public boolean isSlotLocked(int slot) {
        return slotLockMap.getOrDefault(slot, true);
    }

    public abstract void recalculateContents();

    public void openInventory() {
        recalculateContents();
        player.openInventory(inventory);
        InventoryHandler.putInventory(player.getUniqueId(), this);
    }


    // Event Methods
    public void onOpen(InventoryOpenEvent event) {}

    public void onClose(InventoryCloseEvent event) {
        InventoryHandler.removeInventory(player.getUniqueId());
    }

    public void onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        switch (event.getAction()) {
            case DROP_ALL_CURSOR, DROP_ALL_SLOT, DROP_ONE_CURSOR, DROP_ONE_SLOT, COLLECT_TO_CURSOR -> event.setCancelled(true);
            case MOVE_TO_OTHER_INVENTORY -> {
                // TODO: Get list of unlocked slots and check where to put item
                event.setCancelled(true);
            }
            case PLACE_ALL, PLACE_ONE, PLACE_SOME, PICKUP_ALL, PICKUP_HALF, PICKUP_SOME, PICKUP_ONE, SWAP_WITH_CURSOR -> {
                if (isSlotLocked(slot)) {
                    event.setCancelled(true);
                }
            }

        }
    }

    public void onDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }
}
