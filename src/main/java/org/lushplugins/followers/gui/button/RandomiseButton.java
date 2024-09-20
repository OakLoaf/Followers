package org.lushplugins.followers.gui.button;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.lushlib.gui.button.DynamicItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

public class RandomiseButton extends DynamicItemButton {

    public RandomiseButton(Gui gui, Player player, FollowerUser followerUser) {
        super(() -> Followers.getInstance().getConfigManager().getGuiItem("menu-gui", followerUser.isRandomType() ? "random.enabled" : "random.disabled", Material.CONDUIT).asItemStack(player),
            (event) -> {
                boolean isRandom = followerUser.isRandomType();
                followerUser.setRandom(!isRandom);

                if (!isRandom) {
                    followerUser.randomiseFollowerType();
                    ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-changed")
                        .replace("%follower%", "random"));
                }

                gui.refresh(event.getRawSlot());
            });
    }
}
