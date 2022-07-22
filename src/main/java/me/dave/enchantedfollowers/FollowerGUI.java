package me.dave.enchantedfollowers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import me.dave.enchantedfollowers.datamanager.FollowerUser;

import java.util.*;

public class FollowerGUI {
    private final Inventory inventory;
    private final HashSet<UUID> openInvPlayerSet;

    public FollowerGUI(Player player, int page, HashSet<UUID> playerSet) {
        Followers plugin = Followers.getInstance();
        NamespacedKey pageNumKey = new NamespacedKey(plugin, "page");
        this.openInvPlayerSet = playerSet;
        inventory = Bukkit.createInventory(null, 54, "Followers");
        ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta emptyMeta = empty.getItemMeta();
        emptyMeta.getPersistentDataContainer().set(pageNumKey, PersistentDataType.INTEGER, page);
        emptyMeta.setDisplayName("§r");
        empty.setItemMeta(emptyMeta);
        for (int i = 0; i < 18; i++) {
            if (i <= 8) inventory.setItem(i, empty);
            else inventory.setItem(i + 36, empty);
        }
        List<String> followerSet = new ArrayList<>();
        for (String followerName : Followers.followerManager.getFollowers().keySet()) {
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
            headItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + followerName));
            headItem.setItemMeta(headItemMeta);
            inventory.setItem(i + 9, headItem);
        }
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
        if (!followerSet.isEmpty()) {
            ItemStack followerToggle;
            if (followerUser.isFollowerEnabled()) {
                followerToggle = new ItemStack(Material.LIME_WOOL);
                ItemMeta followerToggleMeta = followerToggle.getItemMeta();
                followerToggleMeta.setDisplayName("§eFollower: §aEnabled");
                followerToggle.setItemMeta(followerToggleMeta);
            } else {
                followerToggle = new ItemStack(Material.RED_WOOL);
                ItemMeta followerToggleMeta = followerToggle.getItemMeta();
                followerToggleMeta.setDisplayName("§eFollower: §cDisabled");
                followerToggle.setItemMeta(followerToggleMeta);
            }
            inventory.setItem(49, followerToggle);
        } else {
            ItemStack noFollowers = new ItemStack(Material.BARRIER);
            ItemMeta followerToggleMeta = noFollowers.getItemMeta();
            followerToggleMeta.setDisplayName("§cYou don't own any followers!");
            noFollowers.setItemMeta(followerToggleMeta);
            inventory.setItem(22, noFollowers);
        }

        if (followerSet.size() > page * 36) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName("§eNext Page ->");
            nextPage.setItemMeta(nextPageMeta);
            inventory.setItem(50, nextPage);
        }
        if (page > 1) {
            ItemStack previousPage = new ItemStack(Material.ARROW);
            ItemMeta previousPageMeta = previousPage.getItemMeta();
            previousPageMeta.setDisplayName("§e<- Previous Page");
            previousPage.setItemMeta(previousPageMeta);
            inventory.setItem(48, previousPage);
        }
        if (player.hasPermission("follower.name")) {
            ItemStack followerName = new ItemStack(Material.NAME_TAG);
            ItemMeta followerNameMeta = followerName.getItemMeta();
            followerNameMeta.setDisplayName("§eFollower Name: §f" + followerUser.getDisplayName());
            List<String> lore = new ArrayList<>();
            if (followerUser.isDisplayNameEnabled()) lore.add("§eShown §7§o(Shift-click to Hide)");
            else lore.add("§eHidden §7§o(Shift-click to Show)");
            followerNameMeta.setLore(lore);
            followerName.setItemMeta(followerNameMeta);
            inventory.setItem(45, followerName);
        }
    }

    public void openInventory(Player player) {
        openInvPlayerSet.add(player.getUniqueId());
        player.openInventory(inventory);
    }
}