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
import org.lushplugins.lushlib.gui.button.SimpleItemButton;
import org.lushplugins.lushlib.gui.inventory.GuiFormat;
import org.lushplugins.lushlib.gui.inventory.PagedGui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.utils.DisplayItemStack;

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
        for (Character character : slotMap.keySet()) {
            switch (character) {
                case 'T' -> {
                    Button toggleButton = new FollowerToggleButton(this, player, followerUser);
                    slotMap.get(character).forEach(slot -> addButton(slot, toggleButton));
                }
                case 'N' -> {
                    if (player.hasPermission("follower.name")) {
                        Button nicknameButton = new NicknameButton(this, player, followerUser);
                        slotMap.get(character).forEach(slot -> addButton(slot, nicknameButton));
                    } else {
                        DisplayItemStack borderItem = guiFormat.getItemReference('#');
                        if (borderItem != null) {
                            slotMap.get(character).forEach(slot -> setItem(slot, borderItem.asItemStack()));
                        }
                    }
                }
                case 'R' -> {
                    if (player.hasPermission("follower.random")) {
                        Button randomiseButton = new RandomiseButton(this, player, followerUser);
                        slotMap.get('R').forEach(slot -> addButton(slot, randomiseButton));
                    } else {
                        DisplayItemStack borderItem = guiFormat.getItemReference('#');
                        if (borderItem != null) {
                            slotMap.get(character).forEach(slot -> setItem(slot, borderItem.asItemStack()));
                        }
                    }
                }
                default -> {
                    DisplayItemStack itemReference = guiFormat.getItemReference(character);
                    if (itemReference != null) {
                        slotMap.get(character).forEach(slot -> setItem(slot, itemReference.asItemStack()));
                    }
                }
            }
        }
    }

    @Override
    public void refresh() {
        Player player = this.getPlayer();
        List<String> followerList = Followers.getInstance().getFollowerManager().getFollowerNames().stream()
            .filter((follower) -> player.hasPermission("followers." + follower.toLowerCase().replace(" ", "_")))
            .toList();

        GuiFormat guiFormat = Followers.getInstance().getConfigManager().getGuiFormat();
        int followersPerPage = guiFormat.getCharCount('F');

        TreeMultimap<Character, Integer> slotMap = guiFormat.getSlotMap();
        for (Character character : slotMap.keySet()) {
            switch (character) {
                case 'F' -> {
                    int index = (page - 1) * followersPerPage;
                    for (int slot : slotMap.get(character)) {
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
                }
                case '>' -> {
                    if (followerList.size() > page * followersPerPage) {
                        DisplayItemStack itemReference = guiFormat.getItemReference(character);
                        if (itemReference != null) {
                            slotMap.get(character).forEach(slot -> addButton(slot, new SimpleItemButton(itemReference, (event) -> nextPage())));
                        }
                    } else {
                        DisplayItemStack borderItem = guiFormat.getItemReference('#');
                        if (borderItem != null) {
                            slotMap.get(character).forEach(slot -> {
                                setItem(slot, borderItem.asItemStack());
                                removeButton(slot);
                            });
                        }
                    }
                }
                case '<' -> {
                    if (page > 1) {
                        DisplayItemStack itemReference = guiFormat.getItemReference(character);
                        if (itemReference != null) {
                            slotMap.get(character).forEach(slot -> addButton(slot, new SimpleItemButton(itemReference, (event) -> previousPage())));
                        }
                    } else {
                        DisplayItemStack borderItem = guiFormat.getItemReference('#');
                        if (borderItem != null) {
                            slotMap.get(character).forEach(slot -> {
                                setItem(slot, borderItem.asItemStack());
                                removeButton(slot);
                            });
                        }
                    }
                }
            }
        }

        if (followerList.isEmpty()) {
            setItem(22, Followers.getInstance().getConfigManager().getGuiItem("menu-gui", "no-followers", Material.BARRIER).asItemStack(player));
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