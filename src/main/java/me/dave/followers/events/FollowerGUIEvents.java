package me.dave.followers.events;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.FollowerGUI;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.utils.TextInterface;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class FollowerGUIEvents implements Listener {
    private final HashSet<UUID> openInvPlayerSet;

    public FollowerGUIEvents(HashSet<UUID> playerSet) {
        this.openInvPlayerSet = playerSet;
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
        NamespacedKey pageNumKey = new NamespacedKey(Followers.getInstance(), "page");
        if (clickedItem.isSimilar(Followers.configManager.getGuiItem("no-followers")) || clickedItem.getItemMeta().getPersistentDataContainer().has(pageNumKey, PersistentDataType.INTEGER)) return;
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("follower-toggle.enabled")) || clickedItem.isSimilar(Followers.configManager.getGuiItem("follower-toggle.disabled"))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            if (!followerUser.isFollowerEnabled()) {
                if (followerUser.getFollowerEntity() == null) {
                    followerUser.spawnFollowerEntity();
                    ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
                }
            } else {
                FollowerEntity followerEntity = followerUser.getFollowerEntity();
                if (followerEntity == null) Followers.dataManager.getFollowerUser(playerUUID).setFollowerEnabled(false);
                else followerUser.disableFollowerEntity();
            }
            FollowerGUI followerInv = new FollowerGUI(player, page, openInvPlayerSet);
            followerInv.openInventory(player);
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("next-page"))) {
            FollowerGUI followerInv = new FollowerGUI(player, page + 1, openInvPlayerSet);
            followerInv.openInventory(player);
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("previous-page"))) {
            FollowerGUI followerInv = new FollowerGUI(player, page - 1, openInvPlayerSet);
            followerInv.openInventory(player);
            return;
        } else if (clickedItem.getType() == Material.NAME_TAG && clickedItem.getItemMeta().getDisplayName().startsWith(ChatColorHandler.translateAlternateColorCodes("&eFollower Name:"))) {
            FollowerEntity followerEntity = Followers.dataManager.getFollowerUser(player.getUniqueId()).getFollowerEntity();
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                player.playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 0.6f, 1.0f);
                followerEntity.setDisplayNameVisible(!followerUser.isDisplayNameEnabled());
                FollowerGUI followerInv = new FollowerGUI(player, page, openInvPlayerSet);
                followerInv.openInventory(player);
                return;
            }
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> {
                TextInterface textInterface = new TextInterface();
                textInterface.title("Enter Name:");
                textInterface.placeholder("Enter follower name");
                textInterface.getInput(player, (output) -> {
                    if (output.equals("")) output = " ";
                    String finalOutput = output;
                    Bukkit.getScheduler().runTask(Followers.getInstance(), () -> {
                        if (followerEntity != null) followerEntity.setDisplayName(finalOutput);
                    });
                });
            }, 5L);
            return;
        }
        String followerName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        FollowerGUI followerInv = new FollowerGUI(player, page, openInvPlayerSet);
        followerInv.openInventory(player);
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(playerUUID);
        FollowerEntity followerEntity = followerUser.getFollowerEntity();
        if (followerEntity != null) followerEntity.setFollowerType(followerName);
        else followerUser.spawnFollowerEntity();
        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        new BukkitRunnable() {
            public void run() {
                if (event.getPlayer().getOpenInventory().getType() != InventoryType.CHEST) {
                    UUID playerUUID = event.getPlayer().getUniqueId();
                    openInvPlayerSet.remove(playerUUID);
                }
            }
        }.runTaskLater(Followers.getInstance(), 1);
    }

    private int getPageNum(Inventory inventory) {
        NamespacedKey pageNumKey = new NamespacedKey(Followers.getInstance(), "page");
        ItemStack item = inventory.getItem(0);
        if (item == null) return 0;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return 0;
        if (itemMeta.getPersistentDataContainer().get(pageNumKey, PersistentDataType.INTEGER) == null) return 0;
        return itemMeta.getPersistentDataContainer().get(pageNumKey, PersistentDataType.INTEGER);
    }
}