package org.lushplugins.followers.gui.button;

import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerHandler;
import org.lushplugins.followers.utils.EntityTypeUtils;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.utils.SimpleItemStack;

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

        FollowerHandler followerHandler = Followers.getInstance().getFollowerManager().getFollower(followerName);
        SimpleItemStack displayItem;
        if (followerHandler != null) {
            SimpleItemStack handlerDisplayItem = followerHandler.getDisplayItem();
            SimpleItemStack headItem = followerHandler.getEquipmentSlot(EquipmentSlot.HELMET);
            if (handlerDisplayItem != null && handlerDisplayItem.getType() != Material.AIR) {
                displayItem = handlerDisplayItem;
            } else if (headItem != null && headItem.getType() != Material.AIR) {
                displayItem = headItem;
            } else {
                displayItem = new SimpleItemStack(EntityTypeUtils.getSpawnEgg(followerHandler.getEntityType()));
            }
        } else {
            displayItem = new SimpleItemStack(Material.ARMOR_STAND);
        }

        displayItem.setDisplayName(ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiFollowerFormat()
            .replace("%follower%", followerName)));

        this.itemStack = displayItem.asItemStack();
    }

    public String getFollowerName() {
        return followerName;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return itemStack;
    }
}
