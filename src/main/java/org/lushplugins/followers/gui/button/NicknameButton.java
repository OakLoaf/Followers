package org.lushplugins.followers.gui.button;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.TextInterface;
import org.lushplugins.lushlib.gui.button.DynamicItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

public class NicknameButton extends DynamicItemButton {

    public NicknameButton(Gui gui, Player player, FollowerUser followerUser) {
        super(() -> {
                ExtendedSimpleItemStack item = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", followerUser.isDisplayNameEnabled() ? "nickname.shown" : "nickname.hidden", Material.NAME_TAG);
                item.setDisplayName(item.getDisplayName() != null
                    ? item.getDisplayName().replace("%nickname%", followerUser.getDisplayName())
                    : followerUser.getDisplayName());

                return item.asItemStack(player);
            },
            (event) -> {
                Follower follower = followerUser.getFollower();
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    player.playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 0.6f, 1.0f);
                    followerUser.setDisplayNameEnabled(!followerUser.isDisplayNameEnabled());
                    gui.refresh(event.getRawSlot());
                    return;
                }

                gui.close();

                TextInterface textInterface = new TextInterface();
                String originName = followerUser.getDisplayName();
                textInterface.originName(originName);
                textInterface.title("Enter Name:");
                textInterface.placeholder("Enter follower name");

                Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> textInterface.getInput(player, (output) -> {
                    if (output.isBlank()) {
                        output = Followers.getInstance().getConfigManager().getDefaultNickname();
                    }
                    if (output.equals(originName)) {
                        return;
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
            });
    }
}
