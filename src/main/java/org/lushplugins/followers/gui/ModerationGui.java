package org.lushplugins.followers.gui;

import com.github.retrooper.packetevents.protocol.world.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.config.FollowerHandler;
import org.lushplugins.followers.entity.OwnedFollower;
import org.lushplugins.lushlib.gui.inventory.PagedGui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.utils.SimpleItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModerationGui extends PagedGui {

    public ModerationGui(Player player) {
        super(54, ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiTitle("moderation-gui"), player), player);

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("moderation-gui", "border", Material.GRAY_STAINED_GLASS_PANE).asItemStack(player);
        for (int i = 0; i < 18; i++) {
            setItem(i <= 8 ? i : i + 36, borderItem);
        }
    }

    @Override
    public void refresh() {
        int startPos = (page - 1) * 36;
        int slotIndex = startPos;

        List<OwnedFollower> ownedFollowers = Followers.getInstance().getDataManager().getOwnedFollowers()
            .subList(startPos, startPos + 36);

        for (OwnedFollower follower : ownedFollowers) {
            FollowerHandler followerHandler = follower.getType();
            if (followerHandler == null) {
                continue;
            }

            SimpleItemStack displayItem = followerHandler.getDisplayItemOrSimilar();

            String displayName = follower.getDisplayName();
            if (displayName == null || displayName.equals("Unnamed")) {
                displayName = "&oUnnamed";
            }
            displayItem.setDisplayName(ChatColorHandler.translate("&e" + displayName + " &7- " + follower.getOwner().getName()));

            List<String> lore = new ArrayList<>();
            if (follower.getNameTagEntity() == null) {
                lore.add("&7&o(Follower Name Hidden)");
            }

            Location followerLocation = follower.getEntity().getLocation();
            lore.add("&7&o" + Math.round(followerLocation.getX()) + ", " + Math.round(followerLocation.getY()) + ", " + Math.round(followerLocation.getZ()));
            displayItem.setLore(ChatColorHandler.translate(lore));

            setItem(slotIndex + 9, displayItem.asItemStack());
            slotIndex++;
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        super.onClick(event, true);
    }
}
