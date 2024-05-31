package org.lushplugins.followers.gui.custom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.gui.button.FollowerButton;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.TextInterface;
import org.lushplugins.lushlib.gui.button.DynamicItemButton;
import org.lushplugins.lushlib.gui.button.SimpleItemButton;
import org.lushplugins.lushlib.gui.inventory.PagedGui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.util.List;

public class MenuGui extends PagedGui {
    // TODO: Add static cache (store follower buttons for 1 minute)
    private final FollowerUser followerUser;

    public MenuGui(Player player) {
        super(54, ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiTitle("menu-gui"), player), player);
        followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "border", Material.GRAY_STAINED_GLASS_PANE).asItemStack(player);
        for (int i = 0; i < 18; i++) {
            setItem(i <= 8 ? i : i + 36, borderItem);
        }

        addButton(49, new DynamicItemButton(
                () -> {
                    Follower follower = followerUser.getFollower();
                    if (followerUser.isFollowerEnabled() && follower != null && follower.isAlive()) {
                        return Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "follower-toggle.enabled", Material.LIME_WOOL).asItemStack(this.getPlayer());
                    } else {
                        return Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "follower-toggle.disabled", Material.RED_WOOL).asItemStack(this.getPlayer());
                    }
                },
                (event) -> {
                    String messageKey;
                    if (followerUser.isFollowerEnabled()) {
                        followerUser.disableFollower();
                        messageKey = "follower-despawned";
                    } else {
                        followerUser.spawnFollower();
                        messageKey = "follower-spawned";
                    }


                    ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage(messageKey));
                    refresh(event.getRawSlot());
                }
            ));

        if (player.hasPermission("follower.name")) {
            addButton(45, new DynamicItemButton(
                    () -> {
                        ExtendedSimpleItemStack item = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", followerUser.isDisplayNameEnabled() ? "nickname.shown" : "nickname.hidden", Material.NAME_TAG);
                        item.setDisplayName(item.getDisplayName() != null
                            ? item.getDisplayName().replace("%nickname%", followerUser.getDisplayName())
                            : followerUser.getDisplayName());

                        return item.asItemStack(this.getPlayer());
                    },
                    (event) -> {
                        Follower follower = followerUser.getFollower();
                        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                            player.playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 0.6f, 1.0f);
                            followerUser.setDisplayNameEnabled(!followerUser.isDisplayNameEnabled());
                            refresh(event.getRawSlot());
                            return;
                        }

                        close();

                        TextInterface textInterface = new TextInterface();
                        textInterface.title("Enter Name:");
                        textInterface.placeholder("Enter follower name");

                        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> textInterface.getInput(player, (output) -> {
                            if (output.isBlank()) {
                                output = "Unnamed";
                            }

                            String finalOutput = output;
                            Bukkit.getScheduler().runTask(Followers.getInstance(), () -> {
                                followerUser.setDisplayName(finalOutput);

                                if (follower != null) {
                                    follower.setDisplayName(finalOutput);
                                }
                                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-name-changed")
                                    .replace("%nickname%", finalOutput));
                            });
                        }), 1);
                    }
                ));
        }

        if (player.hasPermission("follower.random")) {
            addButton(46, new DynamicItemButton(
                    () -> Followers.getInstance().getConfigManager().getGuiItem("menu-gui", followerUser.isRandomType() ? "random.enabled" : "random.disabled", Material.CONDUIT).asItemStack(this.getPlayer()),
                    (event) -> {
                        boolean isRandom = followerUser.isRandomType();
                        followerUser.setRandom(!isRandom);

                        if (!isRandom) {
                            followerUser.randomiseFollowerType();
                            ChatColorHandler.sendMessage(this.getPlayer(), Followers.getInstance().getConfigManager().getLangMessage("follower-changed")
                                .replace("%follower%", "random"));
                        }
                    }
                ));
        }
    }

    @Override
    public void refresh() {
        Player player = this.getPlayer();
        List<String> followerList = Followers.getInstance().getFollowerManager().getFollowerNames().stream()
            .filter((follower) -> player.hasPermission("followers." + follower.toLowerCase().replace(" ", "_")))
            .toList();

        int startPos = (page - 1) * 36;
        for (int i = 0; i < 36; i++, startPos++) {
            int slot = i + 9;
            if (startPos >= followerList.size() || followerList.isEmpty()) {
                removeButton(slot);
            } else {
                String followerName = followerList.get(startPos);
                addButton(slot, new FollowerButton(followerName));
            }
        }

        if (followerList.isEmpty()) {
            setItem(22, Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "no-followers", Material.BARRIER).asItemStack(player));
        }

        if (page > 1) {
            addButton(48, new SimpleItemButton(
                Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "previous-page", Material.ARROW),
                (event) -> previousPage()));
        } else {
            removeButton(48);
        }

        if (followerList.size() > page * 36) {
            addButton(50, new SimpleItemButton(
                Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "next-page", Material.ARROW),
                (event) -> nextPage()));
        } else {
            removeButton(50);
        }

        super.refresh();
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        super.onClick(event, true);

        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();
        if (clickedItem == null || slot < 9 || slot > 44) {
            return;
        }

        if (getButton(slot) instanceof FollowerButton button) {
            Player player = this.getPlayer();
            String followerName = button.getFollowerName();

            followerUser.setFollowerType(followerName);
            if (followerUser.isRandomType()) {
                followerUser.setRandom(false);
            }

            Follower follower = followerUser.getFollower();
            if (follower == null) {
                followerUser.spawnFollower();
                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
            } else {
                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-changed")
                    .replace("%follower%", followerName));
            }
        }
    }
}