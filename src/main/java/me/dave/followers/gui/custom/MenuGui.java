package me.dave.followers.gui.custom;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;
import me.dave.followers.gui.abstracts.PagedGui;
import me.dave.followers.utils.TextInterface;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.dave.followers.data.FollowerUser;

import java.util.*;

public class MenuGui extends PagedGui {

    public MenuGui(Player player) {
        super(54, ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiTitle("menu-gui"), player), player);
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = Followers.configManager.getGuiItem("menu-gui", "border", Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 18; i++) {
            if (i <= 8) inventory.setItem(i, borderItem);
            else inventory.setItem(i + 36, borderItem);
        }

        List<String> followerSet = new ArrayList<>();
        for (String followerName : Followers.followerManager.getFollowerNames()) {
            if (!player.hasPermission("followers." + followerName.toLowerCase().replaceAll(" ", "_"))) {
                continue;
            }

            followerSet.add(followerName);
        }

        int setStartPos = (page - 1) * 36;
        for (int i = 0; i < 36; i++, setStartPos++) {
            if (setStartPos >= followerSet.size() || followerSet.isEmpty()) {
                break;
            }

            String followerName = followerSet.get(setStartPos);
            ItemStack headItem = Followers.followerManager.getFollower(followerName).getHead();
            if (headItem == null || headItem.getType() == Material.AIR) {
                headItem = new ItemStack(Material.ARMOR_STAND);
            }

            ItemMeta headItemMeta = headItem.getItemMeta();
            headItemMeta.setDisplayName(ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiFollowerFormat().replaceAll("%follower%", followerName), player));
            headItem.setItemMeta(headItemMeta);
            inventory.setItem(i + 9, headItem);
        }

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
        if (!followerSet.isEmpty()) {
            ItemStack followerToggle;
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (followerUser.isFollowerEnabled() && followerEntity != null && followerEntity.isAlive()) {
                followerToggle = Followers.configManager.getGuiItem("menu-gui", "follower-toggle.enabled", Material.LIME_WOOL);
            } else {
                followerToggle = Followers.configManager.getGuiItem("menu-gui", "follower-toggle.disabled", Material.RED_WOOL);
            }

            inventory.setItem(49, followerToggle);
        } else {
            ItemStack noFollowers = Followers.configManager.getGuiItem("menu-gui", "no-followers", Material.BARRIER);
            inventory.setItem(22, noFollowers);
        }

        if (followerSet.size() > page * 36) {
            ItemStack nextPage = Followers.configManager.getGuiItem("menu-gui", "next-page", Material.ARROW);
            inventory.setItem(50, nextPage);
        }

        if (page > 1) {
            ItemStack previousPage = Followers.configManager.getGuiItem("menu-gui", "previous-page", Material.ARROW);
            inventory.setItem(48, previousPage);
        }

        if (player.hasPermission("follower.name")) {
            ItemStack followerName;
            if (followerUser.isDisplayNameEnabled()) {
                followerName = Followers.configManager.getGuiItem("menu-gui", "nickname.shown", Material.NAME_TAG);
            } else {
                followerName = Followers.configManager.getGuiItem("menu-gui", "nickname.hidden", Material.NAME_TAG);
            }

            ItemMeta itemMeta = followerName.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(itemMeta.getDisplayName().replaceAll("%nickname%", followerUser.getDisplayName()));
                followerName.setItemMeta(itemMeta);
            }

            inventory.setItem(45, followerName);
        }
        if (player.hasPermission("follower.random")) {
            ItemStack followerName;
            if (followerUser.isRandomType()) {
                followerName = Followers.configManager.getGuiItem("menu-gui", "random.enabled", Material.CONDUIT);
            } else {
                followerName = Followers.configManager.getGuiItem("menu-gui", "random.disabled", Material.CONDUIT);
            }

            inventory.setItem(46, followerName);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        super.onClick(event);
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (clickedItem.isSimilar(Followers.configManager.getGuiItem("menu-gui", "no-followers", Material.BARRIER))) {
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("menu-gui", "follower-toggle.enabled", Material.LIME_WOOL))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            followerUser.disableFollowerEntity();
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-despawned"));
            recalculateContents();
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("menu-gui", "follower-toggle.disabled", Material.RED_WOOL))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            followerUser.spawnFollowerEntity();
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
            recalculateContents();
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("menu-gui", "next-page", Material.ARROW))) {
            nextPage();
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("menu-gui", "previous-page", Material.ARROW))) {
            previousPage();
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("menu-gui", "random.enabled", Material.CONDUIT))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            followerUser.setRandom(false);
            recalculateContents();
            return;
        } else if (clickedItem.isSimilar(Followers.configManager.getGuiItem("menu-gui", "random.disabled", Material.CONDUIT))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            followerUser.setRandom(true);
            followerUser.randomizeFollowerType();
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-changed").replaceAll("%follower%", "random"));
            recalculateContents();
            return;
        } else if (event.getRawSlot() == 45 || (clickedItem.getType() == Followers.configManager.getGuiItem("menu-gui", "nickname.shown", Material.NAME_TAG).getType() || (clickedItem.getType() == Followers.configManager.getGuiItem("menu-gui", "nickname.hidden", Material.NAME_TAG).getType())) && clickedItem.getItemMeta().getDisplayName().startsWith(ChatColorHandler.translateAlternateColorCodes("&eFollower Name:"))) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                player.playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 0.6f, 1.0f);
                if (followerEntity != null) {
                    followerEntity.showDisplayName(!followerUser.isDisplayNameEnabled());
                }
                recalculateContents();
                return;
            }
            player.closeInventory();

            TextInterface textInterface = new TextInterface();
            textInterface.title("Enter Name:");
            textInterface.placeholder("Enter follower name");

            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> {
                textInterface.getInput(player, (output) -> {
                    if (output.isBlank()) {
                        output = "Unnamed";
                    }

                    String finalOutput = output;
                    Bukkit.getScheduler().runTask(Followers.getInstance(), () -> {
                        followerUser.setDisplayName(finalOutput);

                        if (followerEntity != null) {
                            followerEntity.setDisplayName(finalOutput);
                        }
                        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-name-changed").replaceAll("%nickname%", finalOutput));
                    });
                });
            }, 1);

            return;
        }

        if (event.getRawSlot() < 9 || event.getRawSlot() > 44) {
            return;
        }

        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
        if (followerUser.isRandomType()) {
            followerUser.setRandom(false);
        }

        FollowerEntity followerEntity = followerUser.getFollowerEntity();
        String followerName = ChatColorHandler.stripColor(clickedItem.getItemMeta().getDisplayName());
        if (followerEntity != null && followerEntity.isAlive()) {
            followerEntity.setType(followerName);
        } else {
            followerUser.setFollowerType(followerName);
            if (followerEntity != null) {
                followerEntity.setType(followerName);
            }

            followerUser.spawnFollowerEntity();
            ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-spawned"));
        }

        ChatColorHandler.sendMessage(player, Followers.configManager.getLangMessage("follower-changed").replaceAll("%follower%", followerName));
        recalculateContents();
    }
}