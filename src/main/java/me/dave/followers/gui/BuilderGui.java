package me.dave.followers.gui;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.data.FollowerHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BuilderGui extends AbstractGui {
    private final Inventory inventory = Bukkit.createInventory(null, 54, ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiTitle()));
    private final FollowerHandler.Builder followerBuilder;
    private final Player player;

    public BuilderGui(Player player, FollowerHandler.Builder followerBuilder) {
        this.player = player;
        this.followerBuilder = followerBuilder;
    }

    public BuilderGui(Player player) {
        this.player = player;
        this.followerBuilder = new FollowerHandler.Builder();
    }

    @Override
    public String getType() {
        return "followers-builder";
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = getBorderItem();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderItem);
        }

        ItemStack airItem = new ItemStack(Material.AIR);

        // Head Item
        inventory.setItem(11, followerBuilder.getHead());
        // Chest Item
        inventory.setItem(20, followerBuilder.getChest());
        // Leggings Item
        inventory.setItem(29, followerBuilder.getLegs());
        // Boots Item
        inventory.setItem(38, followerBuilder.getFeet());
        // Main Hand Item
        inventory.setItem(19, followerBuilder.getMainHand());
        // Off-Hand Item
        inventory.setItem(21, followerBuilder.getOffHand());


        List<ItemStack> buttons = new ArrayList<>();

        // Button Section
        int index = 14;


        for (int i = 14; i < 26; i++) {
            inventory.setItem(i, buttons);
        }

        inventory.setItem(14, airItem);
        inventory.setItem(15, airItem);
        inventory.setItem(16, airItem);
        inventory.setItem(23, airItem);
        inventory.setItem(24, airItem);
        inventory.setItem(25, airItem);

        // Complete Button
        inventory.setItem(41, airItem);
        // Cancel Button
        inventory.setItem(43, airItem);

        Followers.configManager.getGuiItem("builder-name.default", Material.OAK_SIGN);
        Followers.configManager.getGuiItem("builder-name.locked", Material.OAK_SIGN);

        Followers.configManager.getGuiItem("builder-visible.visible", Material.GLASS);
        Followers.configManager.getGuiItem("builder-visible.invisible", Material.WHITE_STAINED_GLASS);
    }

    @Override
    public void openInventory() {
        recalculateContents();
        player.openInventory(inventory);
        InventoryHandler.putInventory(player.getUniqueId(), this);
    }

    public FollowerHandler.Builder getBuilder() {
        return followerBuilder;
    }

    public boolean isNameLocked() {
        return followerBuilder.isNameLocked();
    }

    private void setItem(int slot, ItemStack item, Material def) {
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
}
