package org.lushplugins.followers.gui.button;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.config.FollowerHandler;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.libraries.chatcolor.parsers.ParserTypes;
import org.lushplugins.lushlib.utils.SimpleItemStack;

import java.util.function.Consumer;

public class FollowerButton extends ItemButton {
    private final String followerName;
    private final SimpleItemStack itemStack;

    public FollowerButton(String followerName) {
        this(followerName, event -> {});
    }

    public FollowerButton(String followerName, Consumer<InventoryClickEvent> onClick) {
        super(onClick);
        this.followerName = followerName;

        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerName);
        SimpleItemStack displayItem;
        if (followerHandler != null) {
            displayItem = followerHandler.getDisplayItemOrSimilar();
        } else {
            displayItem = new SimpleItemStack(Material.ARMOR_STAND);
        }

        displayItem.setDisplayName(ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiFollowerFormat()
            .replace("%follower%", followerName), ParserTypes.color()));

        this.itemStack = displayItem;
    }

    public String getFollowerName() {
        return followerName;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return itemStack.asItemStack(player);
    }
}
