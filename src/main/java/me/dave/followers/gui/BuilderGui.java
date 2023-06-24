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

public class BuilderGui extends AbstractGui {
    private final Inventory inventory = Bukkit.createInventory(null, 54, ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiTitle()));
    private final FollowerHandler followerHandler;
    private final Player player;

    public BuilderGui(Player player) {
        this.player = player;
        recalculateContents();
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
        inventory.setItem(11, airItem);

        inventory.setItem(14, airItem);
        inventory.setItem(15, airItem);
        inventory.setItem(16, airItem);

        inventory.setItem(19, airItem);
        inventory.setItem(20, airItem);
        inventory.setItem(21, airItem);

        inventory.setItem(23, airItem);
        inventory.setItem(24, airItem);
        inventory.setItem(25, airItem);

        inventory.setItem(29, airItem);
        inventory.setItem(38, airItem);

        inventory.setItem(41, airItem);
        inventory.setItem(42, airItem);
        inventory.setItem(43, airItem);
    }

    @Override
    public void openInventory() {
        player.openInventory(inventory);
        InventoryHandler.putInventory(player.getUniqueId(), this);
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
