package org.lushplugins.followers.gui.button;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.config.FollowerHandler;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.lushlib.gui.button.DynamicItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

public class FollowerToggleButton extends DynamicItemButton {

    public FollowerToggleButton(Gui gui, Player player, FollowerUser followerUser) {
        super(() -> {
                Follower follower = followerUser.getFollower();
                if (followerUser.isFollowerEnabled() && follower != null && follower.isSpawned()) {
                    return Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "follower-toggle.enabled", Material.LIME_WOOL).asItemStack(player);
                } else {
                    return Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "follower-toggle.disabled", Material.RED_WOOL).asItemStack(player);
                }
            },
            (event) -> {
                String messageKey;
                if (followerUser.isFollowerEnabled()) {
                    followerUser.setFollowerEnabled(false);
                    messageKey = "follower-despawned";
                } else {
                    FollowerHandler followerType = followerUser.getFollowerType();
                    if (followerType == null || !player.hasPermission(followerType.getPermission())) {
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-not-selected"));
                        return;
                    }

                    followerUser.setFollowerEnabled(true);
                    messageKey = "follower-spawned";
                }


                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage(messageKey));
                gui.refresh(event.getRawSlot());
            });
    }
}
