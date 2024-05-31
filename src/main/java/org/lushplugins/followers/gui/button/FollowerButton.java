package org.lushplugins.followers.gui.button;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.util.function.Consumer;

public class FollowerButton extends ItemButton {
    private final String followerName;
    private final ItemStack itemStack;

    public FollowerButton(String followerName) {
        this(followerName, event -> {});
    }

    public FollowerButton(String followerName, Consumer<InventoryClickEvent> onClick) {
        super(onClick);
        this.followerName = followerName;

        ExtendedSimpleItemStack headItem = Followers.getInstance().getFollowerManager().getFollower(followerName).getHead();
        if (headItem == null || headItem.getType() == Material.AIR) {
            headItem = new ExtendedSimpleItemStack(Material.ARMOR_STAND);
        }

        headItem.setDisplayName(ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiFollowerFormat()
            .replace("%follower%", followerName)));

        this.itemStack = headItem.asItemStack();
    }

    public String getFollowerName() {
        return followerName;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return itemStack;
    }
}
