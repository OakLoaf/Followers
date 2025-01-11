package org.lushplugins.followers.gui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.TreeMultimap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.gui.button.FollowerButton;
import org.lushplugins.followers.gui.button.FollowerToggleButton;
import org.lushplugins.followers.gui.button.NicknameButton;
import org.lushplugins.followers.gui.button.RandomiseButton;
import org.lushplugins.lushlib.gui.button.Button;
import org.lushplugins.lushlib.gui.button.LegacySimpleItemButton;
import org.lushplugins.lushlib.gui.inventory.GuiFormat;
import org.lushplugins.lushlib.gui.inventory.PagedGui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.utils.SimpleItemStack;

import java.time.Duration;
import java.util.List;

public class MenuGui extends PagedGui {
    private static final Cache<String, FollowerButton> FOLLOWER_BUTTONS_CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .build();

    private final FollowerUser followerUser;

    public MenuGui(Player player) {
        super(
            Followers.getInstance().getConfigManager().getGuiFormat().getSize(),
            ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiTitle("menu-gui"), player),
            player
        );
        followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);

        GuiFormat guiFormat = Followers.getInstance().getConfigManager().getGuiFormat();
        TreeMultimap<Character, Integer> slotMap = guiFormat.getSlotMap();

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "border", Material.GRAY_STAINED_GLASS_PANE).asItemStack(player);
        slotMap.get('#').forEach(slot -> setItem(slot, borderItem));

        Button toggleButton = new FollowerToggleButton(this, player, followerUser);
        slotMap.get('T').forEach(slot -> addButton(slot, toggleButton));

        if (player.hasPermission("follower.name")) {
            Button nicknameButton = new NicknameButton(this, player, followerUser);
            slotMap.get('N').forEach(slot -> addButton(slot, nicknameButton));
        } else {
            slotMap.get('N').forEach(slot -> setItem(slot, borderItem));
        }

        if (player.hasPermission("follower.random")) {
            Button randomiseButton = new RandomiseButton(this, player, followerUser);
            slotMap.get('R').forEach(slot -> addButton(slot, randomiseButton));
        } else {
            slotMap.get('R').forEach(slot -> setItem(slot, borderItem));
        }
    }

    @Override
    public void refresh() {
        Player player = this.getPlayer();
        List<String> followerList = Followers.getInstance().getFollowerManager().getFollowerNames().stream()
            .filter((follower) -> player.hasPermission("followers." + follower.toLowerCase().replace(" ", "_")))
            .toList();

        GuiFormat guiFormat = Followers.getInstance().getConfigManager().getGuiFormat();
        TreeMultimap<Character, Integer> slotMap = guiFormat.getSlotMap();

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "border", Material.GRAY_STAINED_GLASS_PANE).asItemStack(player);
        int followersPerPage = guiFormat.getCharCount('F');

        int index = (page - 1) * followersPerPage;
        for (int slot : slotMap.get('F')) {
            if (index >= followerList.size() || followerList.isEmpty()) {
                setItem(slot, new ItemStack(Material.AIR));
                removeButton(slot);
            } else {
                String followerName = followerList.get(index);
                FollowerButton button = FOLLOWER_BUTTONS_CACHE.getIfPresent(followerName);
                if (button == null) {
                    button = new FollowerButton(followerName);
                    FOLLOWER_BUTTONS_CACHE.put(followerName, button);
                }

                addButton(slot, button);
            }

            index++;
        }

        if (followerList.isEmpty()) {
            setItem(22, Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "no-followers", Material.BARRIER).asItemStack(player));
        }

        if (page > 1) {
            SimpleItemStack previousPageButton = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "previous-page", Material.ARROW);
            slotMap.get('<').forEach(slot -> addButton(slot, new LegacySimpleItemButton(previousPageButton, (event) -> previousPage())));
        } else {
            slotMap.get('<').forEach(slot -> {
                setItem(slot, borderItem);
                removeButton(slot);
            });
        }

        if (followerList.size() > page * followersPerPage) {
            SimpleItemStack nextPageButton = Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "next-page", Material.ARROW);
            slotMap.get('>').forEach(slot -> addButton(slot, new LegacySimpleItemButton(nextPageButton, (event) -> nextPage())));
        } else {
            slotMap.get('>').forEach(slot -> {
                setItem(slot, borderItem);
                removeButton(slot);
            });
        }

        super.refresh();
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        super.onClick(event, true);

        if (getButton(event.getRawSlot()) instanceof FollowerButton button) {
            Player player = this.getPlayer();
            String followerName = button.getFollowerName();

            followerUser.setFollowerType(followerName);
            if (followerUser.isRandomType()) {
                followerUser.setRandom(false);
            }

            if (!followerUser.isFollowerEnabled()) {
                followerUser.setFollowerEnabled(true);
                refresh(49);
            }

            Follower follower = followerUser.getFollower();
            if (follower == null) {
                followerUser.setFollowerEnabled(true);
                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-spawned"));
                refresh(49);
            } else {
                ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-changed")
                    .replace("%follower%", followerName));
            }
        }
    }

    public static void clearCache() {
        FOLLOWER_BUTTONS_CACHE.invalidateAll();
    }
}