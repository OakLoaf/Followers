package org.enchantedskies.esfollowers.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerArmorStand;
import org.enchantedskies.esfollowers.FollowerGUI;
import org.enchantedskies.esfollowers.datamanager.FollowerUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class FollowerGUIEvents implements Listener {
    private final ESFollowers plugin;
    private final HashSet<UUID> openInvPlayerSet;
    private final NamespacedKey followerKey;
    private final HashMap<UUID, UUID> playerFollowerMap;
    private final HashMap<String, ItemStack> followerSkullMap;

    public FollowerGUIEvents(ESFollowers instance, HashSet<UUID> playerSet, HashMap<UUID, UUID> playerFollowerMap, HashMap<String, ItemStack> followerSkullMap, NamespacedKey followerKey) {
        plugin = instance;
        this.openInvPlayerSet = playerSet;
        this.followerKey = followerKey;
        this.playerFollowerMap = playerFollowerMap;
        this.followerSkullMap = followerSkullMap;
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        if (!openInvPlayerSet.contains(playerUUID)) return;
        event.setCancelled(true);
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;
        int page = getPageNum(clickedInv);
        if (clickedInv.getType() != InventoryType.CHEST) return;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        else if (clickedItem.isSimilar(getEnabledButton()) || clickedItem.isSimilar(getDisabledButton())) {
            FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
            followerUser.setFollowerEnabled(!followerUser.isFollowerEnabled());
            if (followerUser.isFollowerEnabled()) {
                String followerName = followerUser.getFollower();
                if (!playerFollowerMap.containsKey(playerUUID)) {
                    FollowerArmorStand followerArmorStand = new FollowerArmorStand(plugin, followerName, player, followerSkullMap, playerFollowerMap, followerKey);
                    followerArmorStand.startMovement(0.4);
                    playerFollowerMap.put(playerUUID, followerArmorStand.getArmorStand().getUniqueId());
                }
            } else {
                UUID armorStandUUID = playerFollowerMap.get(playerUUID);
                if (armorStandUUID == null) return;
                Bukkit.getEntity(armorStandUUID).remove();
            }
            FollowerGUI followerInv = new FollowerGUI(plugin, player, page, openInvPlayerSet, followerSkullMap);
            followerInv.openInventory(player);
            return;
        } else if (clickedItem.isSimilar(getNextPageButton())) {
            FollowerGUI followerInv = new FollowerGUI(plugin, player, page + 1, openInvPlayerSet, followerSkullMap);
            followerInv.openInventory(player);
            return;
        } else if (clickedItem.isSimilar(getPreviousPageButton())) {
            FollowerGUI followerInv = new FollowerGUI(plugin, player, page - 1, openInvPlayerSet, followerSkullMap);
            followerInv.openInventory(player);
            return;
        }
        String followerName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(player.getUniqueId());
        if (!followerUser.isFollowerEnabled()) {
            followerUser.setFollowerEnabled(true);
        }
        FollowerGUI followerInv = new FollowerGUI(plugin, player, page, openInvPlayerSet, followerSkullMap);
        followerInv.openInventory(player);
        if (playerFollowerMap.containsKey(player.getUniqueId())) {
            UUID armorstandUUID = playerFollowerMap.get(player.getUniqueId());
            new FollowerArmorStand(plugin, followerName, (ArmorStand) Bukkit.getEntity(armorstandUUID), followerSkullMap);
            followerUser.setFollower(followerName);
            return;
        }
        FollowerArmorStand followerArmorStand = new FollowerArmorStand(plugin, followerName, player, followerSkullMap, playerFollowerMap, followerKey);
        followerArmorStand.startMovement(0.4);
        followerUser.setFollower(followerName);
        playerFollowerMap.put(player.getUniqueId(), followerArmorStand.getArmorStand().getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Follower Spawned.");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        new BukkitRunnable() {
            public void run() {
                if (event.getPlayer().getOpenInventory().getType() != InventoryType.CHEST) {
                    UUID playerUUID = event.getPlayer().getUniqueId();
                    if (!openInvPlayerSet.contains(playerUUID)) return;
                    openInvPlayerSet.remove(playerUUID);
                }
            }
        }.runTaskLater(plugin, 1);
    }

    public int getPageNum(Inventory inventory) {
        NamespacedKey pageNumKey = new NamespacedKey(plugin, "page");
        ItemStack item = inventory.getItem(0);
        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta.getPersistentDataContainer().get(pageNumKey, PersistentDataType.INTEGER);
    }

    public ItemStack getNextPageButton() {
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.setDisplayName("§eNext Page ->");
        nextPage.setItemMeta(nextPageMeta);
        return nextPage;
    }

    public ItemStack getPreviousPageButton() {
        ItemStack previousPage = new ItemStack(Material.ARROW);
        ItemMeta previousPageMeta = previousPage.getItemMeta();
        previousPageMeta.setDisplayName("§e<- Previous Page");
        previousPage.setItemMeta(previousPageMeta);
        return previousPage;
    }

    public ItemStack getEnabledButton() {
        ItemStack followerToggle = new ItemStack(Material.LIME_WOOL);
        ItemMeta followerToggleMeta = followerToggle.getItemMeta();
        followerToggleMeta.setDisplayName("§eFollower: §aEnabled");
        followerToggle.setItemMeta(followerToggleMeta);
        return followerToggle;
    }

    public ItemStack getDisabledButton() {
        ItemStack followerToggle = new ItemStack(Material.RED_WOOL);
        ItemMeta followerToggleMeta = followerToggle.getItemMeta();
        followerToggleMeta.setDisplayName("§eFollower: §cDisabled");
        followerToggle.setItemMeta(followerToggleMeta);
        return followerToggle;
    }
}
