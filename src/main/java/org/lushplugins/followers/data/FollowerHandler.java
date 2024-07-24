package org.lushplugins.followers.data;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.meta.types.LivingEntityMeta;
import me.tofaa.entitylib.meta.types.PlayerMeta;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import me.tofaa.entitylib.wrapper.WrapperPlayer;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.Converter;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.SkinData;
import org.lushplugins.followers.utils.SkinUtils;
import org.lushplugins.lushlib.utils.RegistryUtils;

import java.util.*;

public class FollowerHandler {
    private final String name;
    private final EntityType entityType;
    private final Map<EquipmentSlot, ExtendedSimpleItemStack> equipment;
    private final SkinData skin;
    private final boolean isVisible;
    private final Double scale;

    public FollowerHandler(ConfigurationSection configurationSection) {
        this.name = configurationSection.getName();
        this.entityType = SpigotConversionUtil.fromBukkitEntityType(RegistryUtils.fromString(Registry.ENTITY_TYPE, configurationSection.getString("entityType", "armor_stand")));

        this.equipment = new HashMap<>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            String slotName = Converter.getEquipmentSlotName(equipmentSlot);

            if (configurationSection.contains(slotName)) {
                equipment.put(equipmentSlot, new ExtendedSimpleItemStack(configurationSection.getConfigurationSection(slotName)));
            }
        }

        String skinValue = configurationSection.getString("skin");
        String skinSignature = configurationSection.getString("skin-signature");
        if (skinValue != null) {
            if (skinValue.equalsIgnoreCase("mirror")) {
                this.skin = new SkinData("mirror", null);
            } else {
                this.skin = new SkinData(skinValue, skinSignature);

                if (skinSignature == null) {
                    SkinUtils.generateSkin(skinValue).thenAccept(skin -> this.skin.setSignature(skin.data.texture.signature));
                }
            }
        } else {
            this.skin = null;
        }

        this.isVisible = configurationSection.getBoolean("visible", true);
        this.scale = configurationSection.contains("scale") ? configurationSection.getDouble("scale") : null;
    }

    private FollowerHandler(String name, EntityType entityType, Map<EquipmentSlot, ExtendedSimpleItemStack> equipment, SkinData skin, boolean visible, @Nullable Double scale) {
        this.name = name;
        this.entityType = entityType;
        this.equipment = equipment;
        this.skin = skin;
        this.isVisible = visible;
        this.scale = scale;
    }

    public String getName() {
        return name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Map<EquipmentSlot, ExtendedSimpleItemStack> getEquipment() {
        return Collections.unmodifiableMap(equipment);
    }

    public @Nullable ExtendedSimpleItemStack getEquipmentSlot(EquipmentSlot equipmentSlot) {
        return equipment.containsKey(equipmentSlot) ? equipment.get(equipmentSlot).clone() : null;
    }

    public SkinData getSkin() {
        return skin;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public double getScale() {
        return scale != null ? scale : Followers.getInstance().getConfigManager().getDefaultScale();
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
        if (entityType.equals(EntityTypes.PLAYER)) {
            List<TextureProperty> textureProperties = List.of(new TextureProperty("textures", skin.getValue(), skin.getSignature()));

            WrapperPlayer entity = new WrapperPlayer(new UserProfile(uuid, "follower_pet", textureProperties), entityId);
            entity.setInTablist(false);

            PlayerMeta meta = entity.getEntityMeta(PlayerMeta.class);
            meta.setJacketEnabled(true);
            meta.setLeftSleeveEnabled(true);
            meta.setRightSleeveEnabled(true);
            meta.setLeftLegEnabled(true);
            meta.setRightLegEnabled(true);
            meta.setHatEnabled(true);

            return entity;
        } else {
            return new WrapperLivingEntity(entityId, uuid, entityType, entityMeta);
        }
    }

    /**
     * @param entity A spawned entity
     */
    public void applyAttributes(WrapperLivingEntity entity) {
        LivingEntityMeta entityMeta = (LivingEntityMeta) entity.getEntityMeta();
        entityMeta.setInvisible(!isVisible);
        entityMeta.setSilent(true);
        entity.getAttributes().setAttribute(Attributes.GENERIC_SCALE, this.getScale());

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
        private Map<EquipmentSlot, ExtendedSimpleItemStack> equipment;
        private SkinData skin;
        private boolean visible;
        private Double scale;

        public Builder() {
            this.entityType = EntityTypes.ARMOR_STAND;
            this.equipment = new HashMap<>();
            this.visible = true;
            this.scale = null;
        }

        public Builder(FollowerHandler handler) {
            this.name = handler.getName();
            this.entityType = handler.getEntityType();
            this.equipment = new HashMap<>(handler.getEquipment());
            this.skin = handler.getSkin();
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

        public Map<EquipmentSlot, ExtendedSimpleItemStack> getEquipment() {
            return equipment;
        }

        public Builder setEquipment(Map<EquipmentSlot, ExtendedSimpleItemStack> equipment) {
            this.equipment = equipment;
            return this;
        }

        public ExtendedSimpleItemStack getEquipmentSlot(EquipmentSlot slot) {
            return equipment.get(slot);
        }

        public Builder setEquipmentSlot(EquipmentSlot slot, ExtendedSimpleItemStack item) {
            if (item != null) {
                equipment.put(slot, item);
            } else {
                equipment.remove(slot);
            }

            return this;
        }

        public SkinData getSkin() {
            return skin;
        }

        public Builder setSkin(SkinData skin) {
            this.skin = skin;
            return this;
        }

        public boolean isVisible() {
            return visible;
        }

        public Builder setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public @Nullable Double getScale() {
            return scale;
        }

        public Builder setScale(@Nullable Double scale) {
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
            return new FollowerHandler(name, entityType, equipment, skin, visible, scale);
        }
    }
}
