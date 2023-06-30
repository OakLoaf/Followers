package me.dave.followers.gui.custom;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import me.dave.followers.gui.InventoryHandler;
import me.dave.followers.gui.abstracts.PagedGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModerationGui extends PagedGui {
    private final Inventory inventory = Bukkit.createInventory(null, 54, ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiTitle()));
    private final Player player;

    public ModerationGui(Player player) {
        this.player = player;
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = getBorderItem();
        for (int i = 0; i < 18; i++) {
            if (i <= 8) inventory.setItem(i, borderItem);
            else inventory.setItem(i + 36, borderItem);
        }

        List<FollowerEntity> namedFollowerEntities = getActiveNamedFollowers();

        int setStartPos = (page - 1) * 36;
        for (int i = 0; i < 36; i++, setStartPos++) {
            if (setStartPos >= namedFollowerEntities.size() || namedFollowerEntities.isEmpty()) break;

            FollowerEntity followerEntity = namedFollowerEntities.get(setStartPos);
            ItemStack followerItem = followerEntity.getType().getHead();
            if (followerItem == null || followerItem.getType() == Material.AIR) followerItem = new ItemStack(Material.ARMOR_STAND);
            ItemMeta followerMeta = followerItem.getItemMeta();
            followerMeta.setDisplayName(ChatColorHandler.translateAlternateColorCodes("&e" + followerEntity.getDisplayName() + " &7- " + player.getName()));
            Location followerLocation = followerEntity.getLocation();
            followerMeta.setLore(ChatColorHandler.translateAlternateColorCodes(Arrays.asList("&7&o" + followerLocation.getBlockX() + ", " + followerLocation.getBlockY() + ", " + followerLocation.getBlockZ())));
            followerItem.setItemMeta(followerMeta);

            inventory.setItem(i + 9, followerItem);
        }
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
    }

    private List<FollowerEntity> getActiveNamedFollowers() {
        List<FollowerEntity> activeFollowerList = new ArrayList<>();

        Bukkit.getOnlinePlayers().forEach(player -> {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            if (!followerUser.isDisplayNameEnabled()) return;
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (followerEntity == null || !followerEntity.isAlive) return;

            String displayName = followerEntity.getDisplayName();
            if (displayName.equals("Unnamed")) return;

            activeFollowerList.add(followerEntity);
        });

        return activeFollowerList;
    }

    private ItemStack getBorderItem() {
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(ChatColorHandler.translateAlternateColorCodes("&r"));
        borderItem.setItemMeta(borderMeta);
        return borderItem;
    }
}
