package org.enchantedskies.esfollowers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class FollowerGUI {
    private final Inventory inventory;
    private final HashSet<UUID> openInvPlayerSet;

    public FollowerGUI(ESFollowers instance, Player player, HashSet<UUID> playerSet, HashMap<String, ItemStack> followerSkullMap) {
        FileConfiguration config = instance.getConfig();
        this.openInvPlayerSet = playerSet;
        inventory = Bukkit.createInventory(null, 54, "Followers");
        ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta emptyMeta = empty.getItemMeta();
        emptyMeta.setDisplayName("§r");
        empty.setItemMeta(emptyMeta);
        for (int i = 0; i < 18; i++) {
            if (i <= 8) inventory.setItem(i, empty);
            else inventory.setItem(i + 36, empty);
        }
        int i = 8;
        for (String followerName : config.getKeys(false)) {
            if (!player.hasPermission("followers." + followerName.toLowerCase())) continue;
            i += 1;
            ConfigurationSection configSection = config.getConfigurationSection(followerName + ".Head");
            if (configSection == null) continue;
            String materialStr = configSection.getString("Material", "");
            Material material = Material.getMaterial(materialStr.toUpperCase());
            if (material == null) continue;
            ItemStack item = new ItemStack(material);
            if (material == Material.PLAYER_HEAD) item = followerSkullMap.get(followerName);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + followerName));
            item.setItemMeta(itemMeta);
            inventory.setItem(i, item);
        }
    }

    public void openInventory(Player player) {
        openInvPlayerSet.add(player.getUniqueId());
        player.openInventory(inventory);
    }
}