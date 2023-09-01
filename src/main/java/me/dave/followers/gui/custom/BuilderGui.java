package me.dave.followers.gui.custom;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.data.FollowerHandler;
import me.dave.followers.exceptions.ObjectNameLockedException;
import me.dave.followers.gui.InventoryHandler;
import me.dave.followers.gui.abstracts.AbstractGui;
import me.dave.followers.utils.TextInterface;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BuilderGui extends AbstractGui {
    private static final Map<Integer, EquipmentSlot> slotToEquipmentSlot = Map.ofEntries(
        Map.entry(11, EquipmentSlot.HEAD),
        Map.entry(20, EquipmentSlot.CHEST),
        Map.entry(29, EquipmentSlot.LEGS),
        Map.entry(38, EquipmentSlot.FEET),
        Map.entry(19, EquipmentSlot.HAND),
        Map.entry(21, EquipmentSlot.OFF_HAND)
    );
    private final FollowerHandler.Builder followerBuilder;
    private final Mode mode;

    public BuilderGui(Player player, Mode mode, FollowerHandler.Builder followerBuilder) {
        super(54, ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiTitle("builder-gui")), player);
        this.mode = mode;
        this.followerBuilder = followerBuilder;

        this.unlockSlots(11, 19, 20, 21, 29, 38);
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = Followers.configManager.getGuiItem("builder-gui", "border", Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderItem);
        }

        slotToEquipmentSlot.forEach((slot, equipmentSlot) -> setItem(slot, followerBuilder.getSlot(equipmentSlot)));

        List<ItemStack> buttons = new ArrayList<>();

        ItemStack nameButtonItem;
        if (!followerBuilder.isNameLocked()) {
            nameButtonItem = Followers.configManager.getGuiItem("builder-gui", "name-button.default", Material.OAK_SIGN);
        } else {
            nameButtonItem = Followers.configManager.getGuiItem("builder-gui", "name-button.locked", Material.OAK_SIGN);
        }

        ItemMeta itemMeta = nameButtonItem.getItemMeta();
        if (followerBuilder.getName() != null) {
            itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%name%", followerBuilder.getName()));
        } else {
            itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%name%", ChatColorHandler.translateAlternateColorCodes("&c&oUnnamed")));
        }
        nameButtonItem.setItemMeta(itemMeta);
        buttons.add(nameButtonItem);


        if (followerBuilder.isVisible()) {
            buttons.add(Followers.configManager.getGuiItem("builder-gui", "visibility-button.visible", Material.WHITE_STAINED_GLASS));
        } else {
            buttons.add(Followers.configManager.getGuiItem("builder-gui", "visibility-button.invisible", Material.GLASS));
        }

        // Button Section
        List<Integer> buttonSlots = new LinkedList<>(Arrays.asList(14, 15, 16, 23, 24, 25));
        buttons.forEach(button -> {
            if (buttonSlots.isEmpty()) {
                return;
            }
            inventory.setItem(buttonSlots.remove(0), button);
        });

        // Complete Button
        inventory.setItem(41, Followers.configManager.getGuiItem("builder-gui", "complete-button", Material.LIME_WOOL));
        // Cancel Button
        inventory.setItem(43, Followers.configManager.getGuiItem("builder-gui", "cancel-button", Material.RED_WOOL));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return;
        }

        super.onClick(event);

        if (!event.getClickedInventory().equals(inventory)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        if (clickedItem == null && cursorItem == null) {
            return;
        }

        int slot = event.getRawSlot();
        switch(event.getAction()) {
            case PLACE_ALL -> {
                if (slotToEquipmentSlot.containsKey(slot)) {
                    event.setCancelled(true);
                    followerBuilder.setSlot(slotToEquipmentSlot.get(slot), cursorItem);
                }
            }
            case PICKUP_ALL, SWAP_WITH_CURSOR -> {
                if (slotToEquipmentSlot.containsKey(slot)) {
                    event.setCancelled(true);
                    followerBuilder.setSlot(slotToEquipmentSlot.get(slot), null);
                }
            }
            case PLACE_SOME, PLACE_ONE, PICKUP_HALF, PICKUP_SOME, PICKUP_ONE -> {
                if (slotToEquipmentSlot.containsKey(slot)) {
                    event.setCancelled(true);
                }
            }
            case NOTHING, UNKNOWN, DROP_ALL_SLOT, DROP_ONE_SLOT, DROP_ALL_CURSOR, DROP_ONE_CURSOR, CLONE_STACK, HOTBAR_SWAP, HOTBAR_MOVE_AND_READD, COLLECT_TO_CURSOR, MOVE_TO_OTHER_INVENTORY -> {}
        }

        Player player = (Player) event.getWhoClicked();

        ItemStack nameButtonItem = Followers.configManager.getGuiItem("builder-gui", "name-button.default", Material.OAK_SIGN);
        ItemMeta itemMeta = nameButtonItem.getItemMeta();
        if (followerBuilder.getName() != null) {
            itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%name%", followerBuilder.getName()));
        } else {
            itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%name%", ChatColorHandler.translateAlternateColorCodes("&c&oUnnamed")));
        }

        nameButtonItem.setItemMeta(itemMeta);

        if (clickedItem == null) {
            recalculateContents();
            return;
        }

        if (clickedItem.isSimilar(nameButtonItem)) {
            player.closeInventory();
            TextInterface textInterface = new TextInterface();
            textInterface.title("Enter Name:");
            textInterface.placeholder("Enter follower name");
            textInterface.getInput(player, (output) -> {
                if (output.equals("")) {
                    ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-no-name"));
                    return;
                }
                String finalOutput = output.replaceAll("\\.", "-");
                Bukkit.getScheduler().runTask(Followers.getInstance(), () -> {
                    try {
                        followerBuilder.setName(finalOutput);
                    } catch (ObjectNameLockedException ignored) {}

                    openInventory();
                });
            });
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-gui", "visibility-button.visible", Material.WHITE_STAINED_GLASS))) {
            followerBuilder.setVisible(false);
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-gui", "visibility-button.invisible", Material.GLASS))) {
            followerBuilder.setVisible(true);
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-gui", "complete-button", Material.LIME_WOOL))) {
            complete();
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-gui", "cancel-button", Material.RED_WOOL))) {
            player.closeInventory();
            InventoryHandler.removeInventory(player.getUniqueId());
            return;
        }

        recalculateContents();
    }

    @Override
    public void onDrag(InventoryDragEvent event) {
        for (int slot : event.getRawSlots()) {
            if (slot <= 53) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void complete() {
        if (followerBuilder.getName() == null) {
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-no-name"));
            return;
        }

        player.closeInventory();

        if (mode.equals(Mode.CREATE)) {
            Followers.followerManager.createFollower(player, followerBuilder.build());
        } else if (mode.equals(Mode.EDIT)) {
            Followers.followerManager.editFollower(player, followerBuilder.build());
        }
    }

    private ItemStack getBorderItem() {
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(ChatColorHandler.translateAlternateColorCodes("&r"));
        borderItem.setItemMeta(borderMeta);
        return borderItem;
    }

    public enum Mode {
        CREATE,
        EDIT
    }
}
