package me.dave.followers.gui.custom;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;
import me.dave.followers.gui.abstracts.PagedGui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ModerationGui extends PagedGui {

    public ModerationGui(Player player) {
        super(54, ChatColorHandler.translateAlternateColorCodes(Followers.configManager.getGuiTitle("moderation-gui")), player);
    }

    @Override
    public void recalculateContents() {
        inventory.clear();

        ItemStack borderItem = Followers.configManager.getGuiItem("moderation-gui", "border", Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 18; i++) {
            if (i <= 8) {
                inventory.setItem(i, borderItem);
            } else {
                inventory.setItem(i + 36, borderItem);
            }
        }

        List<FollowerEntity> namedFollowerEntities = Followers.dataManager.getAllFollowerEntities();

        int setStartPos = (page - 1) * 36;
        for (int i = 0; i < 36; i++, setStartPos++) {
            if (setStartPos >= namedFollowerEntities.size() || namedFollowerEntities.isEmpty()) {
                break;
            }

            FollowerEntity followerEntity = namedFollowerEntities.get(setStartPos);
            ItemStack followerItem = followerEntity.getType().getHead();
            if (followerItem == null || followerItem.getType() == Material.AIR) {
                followerItem = new ItemStack(Material.ARMOR_STAND);
            }

            ItemMeta followerMeta = followerItem.getItemMeta();

            String displayName = followerEntity.getDisplayName();
            if (displayName.equals("Unnamed")) {
                displayName = "&oUnnamed";
            }
            followerMeta.setDisplayName(ChatColorHandler.translateAlternateColorCodes("&e" + displayName + " &7- " + followerEntity.getPlayer().getName()));

            List<String> lore = new ArrayList<>();
            if (!followerEntity.isDisplayNameVisible()) {
                lore.add("&7&o(Follower Name Hidden)");
            }
            Location followerLocation = followerEntity.getLocation();
            lore.add("&7&o" + followerLocation.getBlockX() + ", " + followerLocation.getBlockY() + ", " + followerLocation.getBlockZ());
            followerMeta.setLore(ChatColorHandler.translateAlternateColorCodes(lore));

            followerItem.setItemMeta(followerMeta);
            inventory.setItem(i + 9, followerItem);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
