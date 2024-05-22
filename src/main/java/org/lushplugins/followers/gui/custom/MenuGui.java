package org.lushplugins.followers.gui.custom;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;
import org.lushplugins.followers.gui.abstracts.PagedGui;
import org.lushplugins.followers.utils.TextInterface;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.util.*;

public class MenuGui extends PagedGui {

    public MenuGui(Player player) {
        super(54, ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiTitle("menu-gui"), player), player);
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "border", Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 18; i++) {
            if (i <= 8) inventory.setItem(i, borderItem);
            else inventory.setItem(i + 36, borderItem);
        }

        List<String> followerSet = new ArrayList<>();
        for (String followerName : Followers.getInstance().getFollowerManager().getFollowerNames()) {
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
            ItemStack headItem = Followers.getInstance().getFollowerManager().getFollower(followerName).getHead().asItemStack();
            if (headItem == null || headItem.getType() == Material.AIR) {
                headItem = new ItemStack(Material.ARMOR_STAND);
            }

            ItemMeta headItemMeta = headItem.getItemMeta();
            headItemMeta.setDisplayName(ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiFollowerFormat().replaceAll("%follower%", followerName), player));
            headItem.setItemMeta(headItemMeta);
            inventory.setItem(i + 9, headItem);
        }

        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
        if (!followerSet.isEmpty()) {
            ItemStack followerToggle;
            FollowerEntity followerEntity = followerUser.getFollowerEntity();
            if (followerUser.isFollowerEnabled() && followerEntity != null && followerEntity.isAlive()) {
                followerToggle = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "follower-toggle.enabled", Material.LIME_WOOL);
            } else {
                followerToggle = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "follower-toggle.disabled", Material.RED_WOOL);
            }

            inventory.setItem(49, followerToggle);
        } else {
            ItemStack noFollowers = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "no-followers", Material.BARRIER);
            inventory.setItem(22, noFollowers);
        }

        if (followerSet.size() > page * 36) {
            ItemStack nextPage = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "next-page", Material.ARROW);
            inventory.setItem(50, nextPage);
        }

        if (page > 1) {
            ItemStack previousPage = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "previous-page", Material.ARROW);
            inventory.setItem(48, previousPage);
        }

        if (player.hasPermission("follower.name")) {
            ItemStack followerName;
            if (followerUser.isDisplayNameEnabled()) {
                followerName = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "nickname.shown", Material.NAME_TAG);
            } else {
                followerName = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "nickname.hidden", Material.NAME_TAG);
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
                followerName = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "random.enabled", Material.CONDUIT);
            } else {
                followerName = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "random.disabled", Material.CONDUIT);
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

        if (clickedItem.isSimilar(Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "no-followers", Material.BARRIER))) {
            return;
        } else if (clickedItem.isSimilar(Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "follower-toggle.enabled", Material.LIME_WOOL))) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.disableFollowerEntity();
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-despawned"));
            recalculateContents();
            return;
        } else if (clickedItem.isSimilar(Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "follower-toggle.disabled", Material.RED_WOOL))) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.spawnFollowerEntity();
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
            recalculateContents();
            return;
        } else if (clickedItem.isSimilar(Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "next-page", Material.ARROW))) {
            nextPage();
            return;
        } else if (clickedItem.isSimilar(Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "previous-page", Material.ARROW))) {
            previousPage();
            return;
        } else if (clickedItem.isSimilar(Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "random.enabled", Material.CONDUIT))) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.setRandom(false);
            recalculateContents();
            return;
        } else if (clickedItem.isSimilar(Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "random.disabled", Material.CONDUIT))) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            followerUser.setRandom(true);
            followerUser.randomizeFollowerType();
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-changed").replaceAll("%follower%", "random"));
            recalculateContents();
            return;
        } else if (event.getRawSlot() == 45 || (clickedItem.getType() == Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "nickname.shown", Material.NAME_TAG).getType() || (clickedItem.getType() == Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "nickname.hidden", Material.NAME_TAG).getType())) && clickedItem.getItemMeta().getDisplayName().startsWith(ChatColorHandler.translate("&eFollower Name:"))) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
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
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-name-changed").replaceAll("%nickname%", finalOutput));
                    });
                });
            }, 1);

            return;
        }

        if (event.getRawSlot() < 9 || event.getRawSlot() > 44) {
            return;
        }

        FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
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
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
        }

        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-changed").replaceAll("%follower%", followerName));
        recalculateContents();
    }
}