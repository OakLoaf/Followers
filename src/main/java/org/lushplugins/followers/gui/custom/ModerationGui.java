package org.lushplugins.followers.gui.custom;

import com.github.retrooper.packetevents.protocol.world.Location;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;
import org.lushplugins.followers.gui.abstracts.PagedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import java.util.ArrayList;
import java.util.List;

public class ModerationGui extends PagedGui {

    public ModerationGui(Player player) {
        super(54, ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiTitle("moderation-gui"), player), player);
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("moderation-gui", "border", Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 18; i++) {
            if (i <= 8) {
                inventory.setItem(i, borderItem);
            } else {
                inventory.setItem(i + 36, borderItem);
            }
        }

        List<FollowerEntity> namedFollowerEntities = Followers.getInstance().getDataManager().getAllFollowerEntities();

        int setStartPos = (page - 1) * 36;
        for (int i = 0; i < 36; i++, setStartPos++) {
            if (setStartPos >= namedFollowerEntities.size() || namedFollowerEntities.isEmpty()) {
                break;
            }

            FollowerEntity followerEntity = namedFollowerEntities.get(setStartPos);
            ItemStack followerItem = followerEntity.getType().getHead().asItemStack();
            if (followerItem == null || followerItem.getType() == Material.AIR) {
                followerItem = new ItemStack(Material.ARMOR_STAND);
            }

            ItemMeta followerMeta = followerItem.getItemMeta();

            String displayName = followerEntity.getDisplayName();
            if (displayName.equals("Unnamed")) {
                displayName = "&oUnnamed";
            }
            followerMeta.setDisplayName(ChatColorHandler.translate("&e" + displayName + " &7- " + followerEntity.getPlayer().getName()));

            List<String> lore = new ArrayList<>();
            if (!followerEntity.isDisplayNameVisible()) {
                lore.add("&7&o(Follower Name Hidden)");
            }

            Location followerLocation = followerEntity.getEntity().getLocation();
            lore.add("&7&o" + Math.round(followerLocation.getX()) + ", " + Math.round(followerLocation.getY()) + ", " + Math.round(followerLocation.getZ()));
            followerMeta.setLore(ChatColorHandler.translate(lore));

            followerItem.setItemMeta(followerMeta);
            inventory.setItem(i + 9, followerItem);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
