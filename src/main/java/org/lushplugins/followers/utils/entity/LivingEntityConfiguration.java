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
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.Converter;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.lushlib.gui.button.ItemButton;

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

        this.scale = config.contains("scale") ? config.getDouble("scale") : null;
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

    public void setEquipment(EquipmentSlot slot, ExtendedSimpleItemStack item) {
        equipment.put(slot, item);
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    @Override
    public List<ItemButton> getGuiButtons() {
        List<ItemButton> buttons = new ArrayList<>(super.getGuiButtons());

        // TODO: Add buttons

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

            livingEntity.getAttributes().setAttribute(Attributes.GENERIC_SCALE, this.getScale());
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
