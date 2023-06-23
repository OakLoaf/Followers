package me.dave.followers.gui;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import me.dave.followers.data.FollowerUser;

import java.util.*;

public class MenuGui extends AbstractGui {
    private final Inventory inventory = Bukkit.createInventory(null, 54, ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiTitle()));
    private final Player player;
    private int page = 1;

    public MenuGui(Player player) {
        this.player = player;
        recalculateContents();
    }

    @Override
    public String getType() {
        return "followers-menu";
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = getBorderItem();
        for (int i = 0; i < 18; i++) {
            if (i <= 8) inventory.setItem(i, borderItem);
            else inventory.setItem(i + 36, borderItem);
        }

        List<String> followerSet = new ArrayList<>();
        for (String followerName : Followers.followerManager.getFollowerNames()) {
            if (!player.hasPermission("followers." + followerName.toLowerCase().replaceAll(" ", "_"))) continue;
            followerSet.add(followerName);
        }
        int setStartPos = (page - 1) * 36;
        for (int i = 0; i < 36; i++, setStartPos++) {
            if (setStartPos >= followerSet.size() || followerSet.isEmpty()) break;
            String followerName = followerSet.get(setStartPos);
            ItemStack headItem = Followers.followerManager.getFollower(followerName).getHead();
            if (headItem == null || headItem.getType() == Material.AIR) headItem = new ItemStack(Material.ARMOR_STAND);
            ItemMeta headItemMeta = headItem.getItemMeta();
            headItemMeta.setDisplayName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiFollowerFormat().replaceAll("%follower%", followerName)));
            headItem.setItemMeta(headItemMeta);
            inventory.setItem(i + 9, headItem);
        }
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
        if (!followerSet.isEmpty()) {
            ItemStack followerToggle;
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (followerUser.isFollowerEnabled() && followerEntity != null && followerEntity.isAlive) followerToggle = Followers.configManager.getGuiItem("follower-toggle.enabled", Material.LIME_WOOL);
            else followerToggle = Followers.configManager.getGuiItem("follower-toggle.disabled", Material.RED_WOOL);
            inventory.setItem(49, followerToggle);
        } else {
            ItemStack noFollowers = Followers.configManager.getGuiItem("no-followers", Material.BARRIER);
            inventory.setItem(22, noFollowers);
        }

        if (followerSet.size() > page * 36) {
            ItemStack nextPage = Followers.configManager.getGuiItem("next-page", Material.ARROW);
            inventory.setItem(50, nextPage);
        }
        if (page > 1) {
            ItemStack previousPage = Followers.configManager.getGuiItem("previous-page", Material.ARROW);
            inventory.setItem(48, previousPage);
        }
        if (player.hasPermission("follower.name")) {
            ItemStack followerName;
            if (followerUser.isDisplayNameEnabled()) followerName = Followers.configManager.getGuiItem("nickname.shown", Material.NAME_TAG);
            else followerName = Followers.configManager.getGuiItem("nickname.hidden", Material.NAME_TAG);

            ItemMeta itemMeta = followerName.getItemMeta();
            itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%nickname%", followerUser.getDisplayName()));
            followerName.setItemMeta(itemMeta);

            inventory.setItem(45, followerName);
        }
        if (player.hasPermission("follower.random")) {
            ItemStack followerName;
            if (followerUser.isRandomType()) followerName = Followers.configManager.getGuiItem("random.enabled", Material.CONDUIT);
            else followerName = Followers.configManager.getGuiItem("random.disabled", Material.CONDUIT);
            inventory.setItem(46, followerName);
        }
    }

    @Override
    public void openInventory() {
        player.openInventory(inventory);
        InventoryHandler.putInventory(player.getUniqueId(), this);
    }

    public void setPage(int page) {
        this.page = page;
        recalculateContents();
    }

    public void nextPage() {
        setPage(++page);
    }

    public void previousPage() {
        setPage(--page);
    }

    private ItemStack getBorderItem() {
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        NamespacedKey pageNumKey = new NamespacedKey(Followers.getInstance(), "page");
        borderMeta.getPersistentDataContainer().set(pageNumKey, PersistentDataType.INTEGER, page);
        borderMeta.setDisplayName(ChatColorHandler.translateAlternateColorCodes("&r"));
        borderItem.setItemMeta(borderMeta);
        return borderItem;
    }
}