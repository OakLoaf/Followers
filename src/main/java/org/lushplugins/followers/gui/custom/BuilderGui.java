package org.lushplugins.followers.gui.custom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerHandler;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.TextInterface;
import org.lushplugins.lushlib.gui.button.DynamicItemButton;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.utils.RegistryUtils;

import java.util.*;

public class BuilderGui extends Gui {
    private static final Map<Integer, EquipmentSlot> EQUIPMENT_SLOT_MAP = Map.ofEntries(
        Map.entry(11, EquipmentSlot.HEAD),
        Map.entry(20, EquipmentSlot.CHEST),
        Map.entry(29, EquipmentSlot.LEGS),
        Map.entry(38, EquipmentSlot.FEET),
        Map.entry(19, EquipmentSlot.HAND),
        Map.entry(21, EquipmentSlot.OFF_HAND),
        Map.entry(10, EquipmentSlot.BODY)
    );

    private final FollowerHandler.Builder followerBuilder;
    private final Mode mode;

    public BuilderGui(Player player, Mode mode, FollowerHandler.Builder followerBuilder) {
        super(54, ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiTitle("builder-gui"), player), player);
        this.mode = mode;
        this.followerBuilder = followerBuilder;

        EQUIPMENT_SLOT_MAP.keySet().forEach(this::unlockSlot);

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "border", Material.GRAY_STAINED_GLASS_PANE).asItemStack();
        for (int i = 0; i < 54; i++) {
            setItem(i, borderItem);
        }

        EQUIPMENT_SLOT_MAP.forEach((slot, equipmentSlot) -> addButton(
            slot,
            new DynamicItemButton(
                () -> {
                    ExtendedSimpleItemStack item = followerBuilder.getEquipmentSlot(equipmentSlot);
                    return item != null ? item.asItemStack(player) : new ItemStack(Material.AIR);
                },
                (event) -> {
                    ItemStack clickedItem = event.getCurrentItem();
                    ItemStack cursorItem = event.getCursor();
                    if (clickedItem == null && cursorItem == null) {
                        return;
                    }

                    switch (event.getAction()) {
                        case PLACE_ALL -> {
                            if (EQUIPMENT_SLOT_MAP.containsKey(slot)) {
                                event.setCancelled(true);
                                followerBuilder.setEquipmentSlot(EQUIPMENT_SLOT_MAP.get(slot), new ExtendedSimpleItemStack(cursorItem));
                                refresh(slot);
                            }
                        }
                        case PICKUP_ALL, SWAP_WITH_CURSOR -> {
                            if (EQUIPMENT_SLOT_MAP.containsKey(slot)) {
                                event.setCancelled(true);
                                followerBuilder.setEquipmentSlot(EQUIPMENT_SLOT_MAP.get(slot), null);
                                refresh(slot);
                            }
                        }
                        case PLACE_SOME, PLACE_ONE, PICKUP_HALF, PICKUP_SOME, PICKUP_ONE -> {
                            if (EQUIPMENT_SLOT_MAP.containsKey(slot)) {
                                event.setCancelled(true);
                            }
                        }
                        case NOTHING, UNKNOWN, DROP_ALL_SLOT, DROP_ONE_SLOT, DROP_ALL_CURSOR, DROP_ONE_CURSOR, CLONE_STACK, HOTBAR_SWAP, HOTBAR_MOVE_AND_READD, COLLECT_TO_CURSOR, MOVE_TO_OTHER_INVENTORY -> {
                        }
                    }
                }
            )));

        List<ItemButton> buttons = List.of(
            new DynamicItemButton(
                () -> {
                    ExtendedSimpleItemStack nametagButton = Followers.getInstance().getConfigManager().getGuiItem("builder-gui", followerBuilder.isNameLocked() ? "name-button.locked" : "name-button.default", Material.OAK_SIGN);
                    nametagButton.setDisplayName(nametagButton.getDisplayName() != null
                        ? nametagButton.getDisplayName().replace("%name%", followerBuilder.getName() != null ? followerBuilder.getName() : ChatColorHandler.translate("&c&oUnnamed"))
                        : followerBuilder.getName());

                    return nametagButton.asItemStack();
                },
                (event) -> {
                    close();

                    TextInterface textInterface = new TextInterface();
                    textInterface.title("Enter Name:");
                    textInterface.placeholder("Enter follower name");

                    Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> textInterface.getInput(player, (output) -> {
                        if (output.isBlank()) {
                            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-no-name"));
                            return;
                        }

                        String finalOutput = output.replaceAll("\\.", "-");
                        Bukkit.getScheduler().runTask(Followers.getInstance(), () -> {
                            try {
                                followerBuilder.setName(finalOutput);
                            } catch (IllegalStateException ignored) {
                            }

                            open();
                        });
                    }), 1);
                }
            ),
            new DynamicItemButton(
                () -> {
                    String materialRaw = followerBuilder.getEntityType().getName().toString() + "_spawn_egg";
                    try {
                        return new ItemStack(RegistryUtils.fromString(Registry.MATERIAL, materialRaw));
                    } catch (IllegalArgumentException e) {
                        return new ItemStack(Material.ARMOR_STAND);
                    }
                },
                (event) -> {
                    close();

                    TextInterface textInterface = new TextInterface();
                    textInterface.title("Enter Entity Type:");
                    textInterface.placeholder("Entity Type");

                    Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> textInterface.getInput(player, (output) -> {
                        if (output.isBlank()) {
                            // TODO: Replace error message
                            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-no-name"));
                            return;
                        }

                        EntityType entityType = RegistryUtils.fromString(Registry.ENTITY_TYPE, output);
                        if (entityType != null) {
                            Bukkit.getScheduler().runTask(Followers.getInstance(), () -> {
                                followerBuilder.setEntityType(entityType);
                                open();
                            });
                        }
                    }), 1);
                }
            ),
            new DynamicItemButton(
                () -> {
                    if (followerBuilder.isVisible()) {
                        return Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "visibility-button.visible", Material.WHITE_STAINED_GLASS).asItemStack();
                    } else {
                        return Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "visibility-button.invisible", Material.GLASS).asItemStack();
                    }
                },
                (event) -> {
                    followerBuilder.setVisible(!followerBuilder.isVisible());
                    refresh(event.getRawSlot());
                }
            ));

        List<Integer> buttonSlots = new LinkedList<>(Arrays.asList(14, 15, 16, 23, 24, 25));
        buttons.forEach(button -> {
            if (!buttonSlots.isEmpty()) {
                addButton(buttonSlots.remove(0), button);
            }
        });

        addButton(41, Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "complete-button", Material.LIME_WOOL).asItemStack(player), (event) -> complete());
        addButton(43, Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "cancel-button", Material.RED_WOOL).asItemStack(player), (event) -> close());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return;
        }

        super.onClick(event);
    }

    @Override
    public void onDrag(InventoryDragEvent event) {
        for (int slot : event.getRawSlots()) {
            if (slot <= 53) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void complete() {
        Player player = this.getPlayer();
        if (followerBuilder.getName() == null) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-no-name"));
            return;
        }

        player.closeInventory();

        if (mode.equals(Mode.CREATE)) {
            Followers.getInstance().getFollowerManager().createFollower(player, followerBuilder.build());
        } else if (mode.equals(Mode.EDIT)) {
            Followers.getInstance().getFollowerManager().editFollower(player, followerBuilder.build());
        }
    }

    public enum Mode {
        CREATE,
        EDIT
    }
}
