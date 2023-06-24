package me.dave.followers.events;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.exceptions.ObjectNameLockedException;
import me.dave.followers.gui.AbstractGui;
import me.dave.followers.gui.BuilderGui;
import me.dave.followers.gui.MenuGui;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.gui.InventoryHandler;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;

import java.util.UUID;

public class FollowerGuiEvents implements Listener {
    private final NamespacedKey pageNumKey = new NamespacedKey(Followers.getInstance(), "page");

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        AbstractGui playerGui = InventoryHandler.getGui(playerUUID);
        if (playerGui == null) return;

        switch(playerGui.getType()) {
            case "followers-menu" -> onFollowerGuiClick(event, (MenuGui) playerGui);
            case "followers-builder" -> onBuilderGuiClick(event, (BuilderGui) playerGui);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        new BukkitRunnable() {
            public void run() {
                UUID playerUUID = event.getPlayer().getUniqueId();
                InventoryHandler.removeInventory(playerUUID);
            }
        }.runTaskLater(Followers.getInstance(), 1);
    }

    private void onFollowerGuiClick(InventoryClickEvent event, MenuGui followerGui) {
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player player = (Player) event.getWhoClicked();

        if (clickedItem.isSimilar(Followers.configManager.getGuiItem("no-followers", Material.BARRIER)) || clickedItem.getItemMeta().getPersistentDataContainer().has(pageNumKey, PersistentDataType.INTEGER)) return;
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("follower-toggle.enabled", Material.LIME_WOOL))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            followerUser.disableFollowerEntity();
            followerGui.recalculateContents();
            return;
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("follower-toggle.disabled", Material.RED_WOOL))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            followerUser.respawnFollowerEntity();
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
            followerGui.recalculateContents();
            return;
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("next-page", Material.ARROW))) {
            followerGui.nextPage();
            return;
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("previous-page", Material.ARROW))) {
            followerGui.previousPage();
            return;
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("random.enabled", Material.CONDUIT))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            followerUser.setRandom(false);
            followerGui.recalculateContents();
            return;
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("random.disabled", Material.CONDUIT))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            followerUser.setRandom(true);
            followerUser.randomizeFollowerType();
            followerGui.recalculateContents();
            return;
        }
        else if (clickedItem.getType() == Material.NAME_TAG && clickedItem.getItemMeta().getDisplayName().startsWith(ChatColorHandler.translateAlternateColorCodes("&eFollower Name:"))) {
            FollowerEntity followerEntity = Followers.dataManager.getFollowerUser(player.getUniqueId()).getFollowerEntity();
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                player.playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 0.6f, 1.0f);
                followerEntity.setDisplayNameVisible(!followerUser.isDisplayNameEnabled());
                followerGui.recalculateContents();
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

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player.getUniqueId());
        if (followerUser.isRandomType()) followerUser.setRandom(false);
        followerGui.recalculateContents();

        FollowerEntity followerEntity = followerUser.getFollowerEntity();
        String followerName = ChatColorHandler.stripColor(clickedItem.getItemMeta().getDisplayName());
        if (followerEntity != null) followerEntity.setFollowerType(followerName);
        else followerUser.spawnFollowerEntity();

        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
    }

    private void onBuilderGuiClick(InventoryClickEvent event, BuilderGui builderGui) {
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player player = (Player) event.getWhoClicked();

        if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-name.default", Material.OAK_SIGN))) {
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
                        builderGui.getBuilder().setName(finalOutput);
                    } catch (ObjectNameLockedException ignored) {}
                    builderGui.openInventory();
                });
            });
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-visible.visible", Material.GLASS))) {
            builderGui.getBuilder().setVisible(false);
            builderGui.recalculateContents();
        }
        else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("builder-visible.invisible", Material.WHITE_STAINED_GLASS))) {
            builderGui.getBuilder().setVisible(true);
            builderGui.recalculateContents();
        }

        builderGui.recalculateContents();
    }
}