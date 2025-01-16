package org.lushplugins.followers.utils.entity;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.meta.types.LivingEntityMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import me.tofaa.entitylib.wrapper.WrapperEntityEquipment;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.gui.button.StringButton;
import org.lushplugins.followers.utils.Converter;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.utils.DisplayItemStack;

import java.util.*;

public class LivingEntityConfiguration extends EntityConfiguration {
    private final Map<EquipmentSlot, ExtendedSimpleItemStack> equipment;
    private Double scale;

    protected LivingEntityConfiguration(EntityType entityType, ConfigurationSection config) {
        super(entityType, config);

        this.equipment = new HashMap<>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            String slotName = Converter.getEquipmentSlotName(equipmentSlot);

            if (config.contains(slotName)) {
                equipment.put(equipmentSlot, new ExtendedSimpleItemStack(config.getConfigurationSection(slotName)));
            }
        }

        this.scale = config.getDouble("scale", Followers.getInstance().getConfigManager().getDefaultScale());
    }

    protected LivingEntityConfiguration(EntityType entityType) {
        super(entityType);

        this.equipment = new HashMap<>();
        this.scale = null;
    }

    public Map<EquipmentSlot, ExtendedSimpleItemStack> getEquipment() {
        return Collections.unmodifiableMap(equipment);
    }

    public @Nullable ExtendedSimpleItemStack getEquipment(EquipmentSlot slot) {
        return equipment.containsKey(slot) ? equipment.get(slot).clone() : null;
    }

    public void setEquipment(EquipmentSlot slot, @Nullable ExtendedSimpleItemStack item) {
        if (item != null) {
            equipment.put(slot, item);
        } else {
            equipment.remove(slot);
        }
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    @Override
    public List<ItemButton> getGuiButtons(Gui gui) {
        List<ItemButton> buttons = new ArrayList<>(super.getGuiButtons(gui));

        String scaleString = scale != null ? scale.toString() : String.valueOf(Followers.getInstance().getConfigManager().getDefaultScale());
        buttons.add(
            new StringButton(
                scaleString,
                () -> DisplayItemStack.builder(Material.CHAIN)
                    .setDisplayName("&#ffde8aScale: &f" + scaleString)
                    .build()
                    .asItemStack(),
                "Enter Scale:",
                (input) -> {
                    try {
                        Double.valueOf(input);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                (output, player) -> {
                    if (output.isBlank()) {
                        ChatColorHandler.sendMessage(player, "&cThat size is invalid"); // TODO: Configurable message
                        return;
                    }

                    double scale;
                    try {
                        scale = Double.parseDouble(output);
                    } catch (NumberFormatException e) {
                        ChatColorHandler.sendMessage(player, "&cThat size is invalid"); // TODO: Configurable message
                        return;
                    }

                    this.scale = scale;
                    Bukkit.getScheduler().runTask(Followers.getInstance(), gui::open);
                }
            )
        );

        return buttons;
    }

    @Override
    public WrapperEntity createEntity(int entityId, UUID uuid, EntityMeta meta) {
        WrapperLivingEntity entity = new WrapperLivingEntity(entityId, uuid, this.getEntityType(), meta);

        // This can be done on creation as it is hardcoded
        if (entity.getEntityMeta() instanceof LivingEntityMeta livingEntityMeta) {
            livingEntityMeta.setSilent(true);

            if (livingEntityMeta instanceof ArmorStandMeta armorStandMeta) {
                armorStandMeta.setHasNoBasePlate(true);
                armorStandMeta.setHasArms(true);
                armorStandMeta.setSmall(true);

                if (!Followers.getInstance().getConfigManager().areHitboxesEnabled()) {
                    armorStandMeta.setMarker(true);
                }
            }
        }

        return entity;
    }

    @Override
    public void applyAttributes(WrapperEntity entity) {
        super.applyAttributes(entity);

        if (entity instanceof WrapperLivingEntity livingEntity) {
            WrapperEntityEquipment equipment = livingEntity.getEquipment();
            equipment.clearAll();

            for (Map.Entry<EquipmentSlot, ExtendedSimpleItemStack> entry : this.getEquipment().entrySet()) {
                EquipmentSlot slot = entry.getKey();
                ExtendedSimpleItemStack item = entry.getValue();

                if (item != null) {
                    equipment.setItem(
                        slot,
                        SpigotConversionUtil.fromBukkitItemStack(item.asItemStack()));
                }
            }

            Double scale = this.getScale();
            livingEntity.getAttributes().setAttribute(Attributes.GENERIC_SCALE, scale != null ? scale : Followers.getInstance().getConfigManager().getDefaultScale());
        }
    }

    @Override
    public void save(ConfigurationSection config) {
        super.save(config);

        for (Map.Entry<EquipmentSlot, ExtendedSimpleItemStack> entry : equipment.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            ExtendedSimpleItemStack item = entry.getValue();

            item.save(config, Converter.getEquipmentSlotName(slot));
        }

        config.set("scale", scale);
    }
}
