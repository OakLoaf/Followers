package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.enchantedskies.esfollowers.datamanager.FollowerUser;

import java.util.*;

public class FollowerGUI {
    private final Inventory inventory;
    private final HashSet<UUID> openInvPlayerSet;

    public FollowerGUI(ESFollowers instance, Player player, int page, HashSet<UUID> playerSet, HashMap<String, ItemStack> followerSkullMap) {
        NamespacedKey pageNumKey = new NamespacedKey(instance, "page");
        FileConfiguration config = instance.getConfig();
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
        for (String followerName : config.getKeys(false)) {
            if (!player.hasPermission("followers." + followerName.toLowerCase()) && !player.hasPermission("followers.all")) continue;
            followerSet.add(followerName);
        }
        int setStartPos = (page - 1) * 36;
        for (int i = 0; i < 36; i++, setStartPos++) {
            if (setStartPos >= followerSet.size()) break;
            String followerName = followerSet.get(setStartPos);
            ConfigurationSection configSection = config.getConfigurationSection(followerName + ".Head");
            if (configSection == null) continue;
            String materialStr = configSection.getString("Material", "");
            Material material = Material.getMaterial(materialStr.toUpperCase());
            if (material == null) continue;
            ItemStack item = new ItemStack(material);
            if (material == Material.PLAYER_HEAD) item = followerSkullMap.get(followerName);
            if (item == null) item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + followerName));
            item.setItemMeta(itemMeta);
            inventory.setItem(i + 9, item);
        }
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
        if (followerSet.size() != 0) {
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
    }

    public void openInventory(Player player) {
        openInvPlayerSet.add(player.getUniqueId());
        player.openInventory(inventory);
    }
}