package org.lushplugins.followers.data;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.meta.types.LivingEntityMeta;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.lushlib.utils.RegistryUtils;

import java.util.UUID;

public class FollowerHandler {
    private final String name;
    private final EntityType entityType;
    private final ExtendedSimpleItemStack head;
    private final ExtendedSimpleItemStack chest;
    private final ExtendedSimpleItemStack legs;
    private final ExtendedSimpleItemStack feet;
    private final ExtendedSimpleItemStack mainHand;
    private final ExtendedSimpleItemStack offHand;
    private final boolean isVisible;
    private final double scale;

    public FollowerHandler(ConfigurationSection configurationSection) {
        this.name = configurationSection.getName();
        this.entityType = SpigotConversionUtil.fromBukkitEntityType(RegistryUtils.fromString(Registry.ENTITY_TYPE, configurationSection.getString("entityType", "armor_stand")));
        this.head = configurationSection.contains("head") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("head")) : new ExtendedSimpleItemStack(Material.AIR);
        this.chest = configurationSection.contains("chest") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("chest")) : new ExtendedSimpleItemStack(Material.AIR);
        this.legs = configurationSection.contains("legs") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("legs")) : new ExtendedSimpleItemStack(Material.AIR);
        this.feet = configurationSection.contains("feet") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("feet")) : new ExtendedSimpleItemStack(Material.AIR);
        this.mainHand = configurationSection.contains("mainHand") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("mainHand")) : new ExtendedSimpleItemStack(Material.AIR);
        this.offHand = configurationSection.contains("offHand") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("offHand")) : new ExtendedSimpleItemStack(Material.AIR);
        this.isVisible = configurationSection.getBoolean("visible", true);
        this.scale = configurationSection.getDouble("scale", 0.5);
    }

    private FollowerHandler(String name, EntityType entityType, ExtendedSimpleItemStack head, ExtendedSimpleItemStack chest, ExtendedSimpleItemStack legs, ExtendedSimpleItemStack feet, ExtendedSimpleItemStack mainHand, ExtendedSimpleItemStack offHand, boolean visible, double scale) {
        this.name = name;
        this.entityType = entityType;
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.isVisible = visible;
        this.scale = scale;
    }

    public String getName() {
        return name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public ExtendedSimpleItemStack getHead() {
        return head.clone();
    }

    public ExtendedSimpleItemStack getChest() {
        return chest.clone();
    }

    public ExtendedSimpleItemStack getLegs() {
        return legs.clone();
    }

    public ExtendedSimpleItemStack getFeet() {
        return feet.clone();
    }

    public ExtendedSimpleItemStack getMainHand() {
        return mainHand.clone();
    }

    public ExtendedSimpleItemStack getOffHand() {
        return offHand.clone();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public double getScale() {
        return scale;
    }

    public WrapperLivingEntity createEntity() {
        UUID uuid = EntityLib.getPlatform().getEntityUuidProvider().provide(entityType);
        int entityId = EntityLib.getPlatform().getEntityIdProvider().provide(uuid, entityType);

        return createEntity(entityId, uuid);
    }

    public WrapperLivingEntity createEntity(int entityId, UUID uuid) {
        return createEntity(entityId, uuid, EntityMeta.createMeta(entityId, entityType));
    }

    public WrapperLivingEntity createEntity(int entityId, UUID uuid, EntityMeta entityMeta) {
        return new WrapperLivingEntity(entityId, uuid, entityType, entityMeta);
    }

    /**
     * @param entity A spawned entity
     */
    public void applyAttributes(WrapperLivingEntity entity) {
        LivingEntityMeta entityMeta = (LivingEntityMeta) entity.getEntityMeta();
        entityMeta.setInvisible(!isVisible);
        entityMeta.setSilent(true);
        entity.getAttributes().setAttribute(Attributes.GENERIC_SCALE, scale);

        if (entityMeta instanceof ArmorStandMeta armorStandMeta) {
            armorStandMeta.setHasNoBasePlate(true);
            armorStandMeta.setHasArms(true);
            armorStandMeta.setSmall(true);

            if (!Followers.getInstance().getConfigManager().areHitboxesEnabled()) {
                armorStandMeta.setMarker(true);
            }
        }
    }


    public static class Builder {
        private boolean nameLocked = false;
        private String name;
        private EntityType entityType;
        private ExtendedSimpleItemStack head;
        private ExtendedSimpleItemStack chest;
        private ExtendedSimpleItemStack legs;
        private ExtendedSimpleItemStack feet;
        private ExtendedSimpleItemStack mainHand;
        private ExtendedSimpleItemStack offHand;
        private boolean visible;
        private double scale;

        public Builder() {
            this.entityType = EntityTypes.ARMOR_STAND;
            this.head = new ExtendedSimpleItemStack(Material.AIR);
            this.chest = new ExtendedSimpleItemStack(Material.AIR);
            this.legs = new ExtendedSimpleItemStack(Material.AIR);
            this.feet = new ExtendedSimpleItemStack(Material.AIR);
            this.mainHand = new ExtendedSimpleItemStack(Material.AIR);
            this.offHand = new ExtendedSimpleItemStack(Material.AIR);
            this.visible = true;
            this.scale = 0.5;
        }

        public Builder(FollowerHandler handler) {
            this.name = handler.getName();
            this.entityType = handler.getEntityType();
            this.head = handler.getHead();
            this.chest = handler.getChest();
            this.legs = handler.getLegs();
            this.feet = handler.getFeet();
            this.mainHand = handler.getMainHand();
            this.offHand = handler.getOffHand();
            this.visible = handler.isVisible();
            this.scale = handler.getScale();
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) throws IllegalStateException {
            if (nameLocked) {
                throw new IllegalStateException("Object is name locked, name cannot be changed");
            }

            this.name = name;
            return this;
        }

        public EntityType getEntityType() {
            return entityType;
        }

        public Builder setEntityType(org.bukkit.entity.EntityType entityType) {
            return setEntityType(SpigotConversionUtil.fromBukkitEntityType(entityType));
        }

        public Builder setEntityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public ExtendedSimpleItemStack getSlot(@NotNull EquipmentSlot slot) {
            ExtendedSimpleItemStack output = null;
            switch(slot) {
                case HEAD -> output = this.head;
                case CHEST -> output = this.chest;
                case LEGS -> output = this.legs;
                case FEET -> output = this.feet;
                case HAND -> output = this.mainHand;
                case OFF_HAND -> output = this.offHand;
            }
            return output;
        }

        public Builder setSlot(EquipmentSlot slot, @Nullable ExtendedSimpleItemStack item) {
            if (item == null) {
                item = new ExtendedSimpleItemStack(Material.AIR);
            }
            item = item.clone();
            item.setAmount(1);

            switch(slot) {
                case HEAD -> this.head = item;
                case CHEST -> this.chest = item;
                case LEGS -> this.legs = item;
                case FEET -> this.feet = item;
                case HAND -> this.mainHand = item;
                case OFF_HAND -> this.offHand = item;
            }
            return this;
        }

        public boolean isVisible() {
            return visible;
        }

        public Builder setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public double getScale() {
            return scale;
        }

        public Builder setScale(double scale) {
            this.scale = scale;
            return this;
        }

        public boolean isNameLocked() {
            return nameLocked;
        }

        public Builder setNameLocked(boolean locked) {
            this.nameLocked = locked;
            return this;
        }

        public FollowerHandler build() {
            return new FollowerHandler(name, entityType, head, chest, legs, feet, mainHand, offHand, visible, scale);
        }
    }
}
