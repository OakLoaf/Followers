package org.lushplugins.followers.gui;

import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.config.FollowerHandler;
import org.lushplugins.followers.gui.button.StringButton;
import org.lushplugins.followers.utils.EntityTypeUtils;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.StringUtils;
import org.lushplugins.followers.utils.entity.LivingEntityConfiguration;
import org.lushplugins.lushlib.gui.button.DynamicItemButton;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.registry.RegistryUtils;
import org.lushplugins.lushlib.utils.DisplayItemStack;

import java.util.*;

public class BuilderGui extends Gui {
    private static final Map<Integer, EquipmentSlot> EQUIPMENT_SLOT_MAP = Map.ofEntries(
        Map.entry(11, EquipmentSlot.HELMET),
        Map.entry(20, EquipmentSlot.CHEST_PLATE),
        Map.entry(29, EquipmentSlot.LEGGINGS),
        Map.entry(38, EquipmentSlot.BOOTS),
        Map.entry(19, EquipmentSlot.MAIN_HAND),
        Map.entry(21, EquipmentSlot.OFF_HAND)
//        Map.entry(10, EquipmentSlot.BODY)
    );

    private final FollowerHandler.Builder builder;
    private final Mode mode;

    public BuilderGui(Player player, Mode mode, FollowerHandler.Builder builder) {
        super(54, ChatColorHandler.translate(Followers.getInstance().getConfigManager().getGuiTitle("builder-gui"), player), player);
        this.mode = mode;
        this.builder = builder;

        EQUIPMENT_SLOT_MAP.keySet().forEach(this::unlockSlot);

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "border", Material.GRAY_STAINED_GLASS_PANE).asItemStack();
        for (int i = 0; i < 54; i++) {
            setItem(i, borderItem);
        }

        EQUIPMENT_SLOT_MAP.forEach((slot, equipmentSlot) -> addButton(
            slot,
            new DynamicItemButton(
                () -> {
                    if (builder.entityConfig() instanceof LivingEntityConfiguration entityConfig) {
                        ExtendedSimpleItemStack item = entityConfig.getEquipment(equipmentSlot);
                        if (item != null) {
                            return item.asItemStack(player);
                        }
                    }

                    return new ItemStack(Material.AIR);
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

                                if (builder.entityConfig() instanceof LivingEntityConfiguration entityConfig) {
                                    entityConfig.setEquipment(equipmentSlot, new ExtendedSimpleItemStack(cursorItem));
                                    refresh(slot);
                                }
                            }
                        }
                        case PICKUP_ALL, SWAP_WITH_CURSOR -> {
                            if (EQUIPMENT_SLOT_MAP.containsKey(slot)) {
                                event.setCancelled(true);

                                if (builder.entityConfig() instanceof LivingEntityConfiguration entityConfig) {
                                    entityConfig.setEquipment(EQUIPMENT_SLOT_MAP.get(slot), null);
                                    refresh(slot);
                                }
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

        reloadButtons();

        addButton(41, Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "complete-button", Material.LIME_WOOL).asItemStack(player), (event) -> complete());
        addButton(43, Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "cancel-button", Material.RED_WOOL).asItemStack(player), (event) -> close());
    }

    public void reloadButtons() {
        List<Integer> buttonSlots = new LinkedList<>(Arrays.asList(14, 15, 16, 23, 24, 25));
        Player player = this.getPlayer();

        ItemStack borderItem = Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "border", Material.GRAY_STAINED_GLASS_PANE).asItemStack();
        for (int slot : buttonSlots) {
            removeButton(slot);
            setItem(slot, borderItem);
        }

        List<ItemButton> buttons = new ArrayList<>(List.of(
            new StringButton(
                "-",
                () -> {
                    ExtendedSimpleItemStack nametagButton = Followers.getInstance().getConfigManager().getGuiItem("builder-gui", this.builder.nameLocked() ? "name-button.locked" : "name-button.default", Material.OAK_SIGN);
                    nametagButton.setDisplayName(nametagButton.getDisplayName() != null
                        ? nametagButton.getDisplayName().replace("%name%", this.builder.name() != null ? this.builder.name() : ChatColorHandler.translate("&c&oUnnamed"))
                        : this.builder.name());

                    return nametagButton.asItemStack();
                },
                "Enter Follower Name:",
                (input) -> true,
                (output, clicker) -> {
                    if (!output.isBlank()) {
                        if (output.charAt(0) == '-') {
                            output = output.substring(1);
                        }
                        String finalOutput = output.replaceAll("\\.", "-");

                        try {
                            this.builder.name(finalOutput);
                        } catch (IllegalStateException ignored) {
                        }
                    } else {
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-no-name"));
                    }

                    Bukkit.getScheduler().runTask(Followers.getInstance(), this::open);
                }
            ),
            new StringButton(
                builder.entityType().getName().toString()
                    .replace("minecraft:", ""),
                () -> {
                    com.github.retrooper.packetevents.protocol.entity.type.EntityType entityType = this.builder.entityType();
                    String entityTypeRaw = entityType.getName().getKey().toLowerCase();

                    return DisplayItemStack.builder(EntityTypeUtils.getSpawnEgg(entityType))
                        .setDisplayName("&#ffde8aEntity Type: &f" + StringUtils.makeFriendly(entityTypeRaw.replace("_", " ")))
                        .build()
                        .asItemStack();
                },
                "Enter Entity Type:",
                (input) -> {
                    EntityType entityType = RegistryUtils.parseString(input.replace(" ", "_"), Registry.ENTITY_TYPE);
                    return entityType != null;
                },
                (output, clicker) -> {
                    if (output.isBlank()) {
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("invalid-entity-type"));
                        return;
                    }

                    EntityType entityType = RegistryUtils.parseString(output.replace(" ", "_"), Registry.ENTITY_TYPE);
                    if (entityType != null) {
                        this.builder.entityType(SpigotConversionUtil.fromBukkitEntityType(entityType));
                    } else {
                        ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("invalid-entity-type"));
                    }

                    reloadButtons();
                    Bukkit.getScheduler().runTask(Followers.getInstance(), this::open);
                }
            )
        ));

        buttons.addAll(builder.entityConfig().getGuiButtons(this));

        buttons.forEach(button -> {
            if (!buttonSlots.isEmpty()) {
                addButton(buttonSlots.remove(0), button);
            }
        });
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
        if (builder.name() == null) {
            ChatColorHandler.sendMessage(player, Followers.getInstance().getConfigManager().getLangMessage("follower-no-name"));
            return;
        }

        player.closeInventory();

        if (mode.equals(Mode.CREATE)) {
            Followers.getInstance().getFollowerManager().createFollowerType(player, builder.build());
        } else if (mode.equals(Mode.EDIT)) {
            Followers.getInstance().getFollowerManager().editFollowerType(player, builder.build());
        }
    }

    public enum Mode {
        CREATE,
        EDIT
    }
}
