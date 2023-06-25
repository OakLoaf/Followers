package me.dave.followers.gui;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.data.FollowerHandler;
import me.dave.followers.exceptions.ObjectNameLockedException;
import me.dave.followers.utils.TextInterface;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BuilderGui extends AbstractGui {
    private final Inventory inventory = Bukkit.createInventory(null, 54, ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiTitle()));
    private final FollowerHandler.Builder followerBuilder;
    private final Player player;
    private final Mode mode;

    public BuilderGui(Player player, Mode mode, FollowerHandler.Builder followerBuilder) {
        this.player = player;
        this.mode = mode;
        this.followerBuilder = followerBuilder;
    }

    public BuilderGui(Player player, Mode mode) {
        this.player = player;
        this.mode = mode;
        this.followerBuilder = new FollowerHandler.Builder();
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = getBorderItem();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderItem);
        }

        // Head Item
        setItem(11, followerBuilder.getHead());
        // Chest Item
        setItem(20, followerBuilder.getChest());
        // Leggings Item
        setItem(29, followerBuilder.getLegs());
        // Boots Item
        setItem(38, followerBuilder.getFeet());
        // Main Hand Item
        setItem(19, followerBuilder.getMainHand());
        // Off-Hand Item
        setItem(21, followerBuilder.getOffHand());


        List<ItemStack> buttons = new ArrayList<>();

        ItemStack nameButtonItem;
        if (!followerBuilder.isNameLocked()) nameButtonItem = Followers.configManager.getGuiItem("builder-gui", "name-button.default", Material.OAK_SIGN);
        else nameButtonItem = Followers.configManager.getGuiItem("builder-gui", "name-button.locked", Material.OAK_SIGN);

        ItemMeta itemMeta = nameButtonItem.getItemMeta();
        if (followerBuilder.getName() != null) itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%name%", followerBuilder.getName()));
        else itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%name%", ChatColorHandler.translateAlternateColorCodes("&c&oUnnamed")));
        nameButtonItem.setItemMeta(itemMeta);
        buttons.add(nameButtonItem);


        if (followerBuilder.isVisible()) buttons.add(Followers.configManager.getGuiItem("builder-gui", "visibility-button.visible", Material.WHITE_STAINED_GLASS));
        else buttons.add(Followers.configManager.getGuiItem("builder-gui", "visibility-button.invisible", Material.GLASS));

        // Button Section
        List<Integer> buttonSlots = new LinkedList<>(Arrays.asList(14, 15, 16, 23, 24, 25));
        buttons.forEach(button -> {
            if (buttonSlots.isEmpty()) return;
            inventory.setItem(buttonSlots.remove(0), button);
        });

        // Complete Button
        inventory.setItem(41, Followers.configManager.getGuiItem("builder-gui", "complete-button", Material.LIME_WOOL));
        // Cancel Button
        inventory.setItem(43, Followers.configManager.getGuiItem("builder-gui", "cancel-button", Material.RED_WOOL));
    }

    @Override
    public void openInventory() {
        recalculateContents();
        player.openInventory(inventory);
        InventoryHandler.putInventory(player.getUniqueId(), this);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player player = (Player) event.getWhoClicked();

        ItemStack nameButtonItem = Followers.configManager.getGuiItem("builder-gui", "name-button.default", Material.OAK_SIGN);
        ItemMeta itemMeta = nameButtonItem.getItemMeta();
        if (followerBuilder.getName() != null) itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%name%", followerBuilder.getName()));
        else itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%name%", ChatColorHandler.translateAlternateColorCodes("&c&oUnnamed")));
        nameButtonItem.setItemMeta(itemMeta);

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
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-gui", "visibility-button.visible", Material.WHITE_STAINED_GLASS))) {
            followerBuilder.setVisible(false);
            recalculateContents();
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-gui", "visibility-button.invisible", Material.GLASS))) {
            followerBuilder.setVisible(true);
            recalculateContents();
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-gui", "complete-button", Material.LIME_WOOL))) {
            complete();
            return;
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-gui", "cancel-button", Material.RED_WOOL))) {
            player.closeInventory();
            InventoryHandler.removeInventory(player.getUniqueId());
            return;
        }

        recalculateContents();
    }

    public void complete() {
        if (followerBuilder.getName() == null) {
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-no-name"));
            return;
        }

        player.closeInventory();
        InventoryHandler.removeInventory(player.getUniqueId());

        if (mode.equals(Mode.CREATE)) Followers.followerManager.createFollower(player, followerBuilder.build());
        else if (mode.equals(Mode.EDIT)) Followers.followerManager.editFollower(player, followerBuilder.build());
    }

    private void setItem(int slot, ItemStack item) {
        if (item == null) item = new ItemStack(Material.AIR);
        inventory.setItem(slot, item);
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
