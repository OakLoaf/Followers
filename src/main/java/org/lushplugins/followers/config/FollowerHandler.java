package org.lushplugins.followers.config;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.utils.*;
import org.lushplugins.followers.utils.entity.EntityConfiguration;
import org.lushplugins.followers.utils.entity.LivingEntityConfiguration;
import org.lushplugins.followers.utils.entity.PlayerConfiguration;
import org.lushplugins.lushlib.utils.SimpleItemStack;

import java.util.*;

public class FollowerHandler {
    private final String name;
    private final EntityConfiguration entityConfig;
    private final SimpleItemStack displayItem;

    public FollowerHandler(ConfigurationSection configurationSection) {
        this.name = configurationSection.getName();
        this.entityConfig = EntityConfiguration.from(configurationSection);

        if (configurationSection.isConfigurationSection("displayItem")) {
            this.displayItem = new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("displayItem"));
        } else {
            this.displayItem = null;
        }
    }

    private FollowerHandler(String name, @NotNull EntityConfiguration entityConfig, SimpleItemStack displayItem) {
        this.name = name;
        this.entityConfig = entityConfig;
        this.displayItem = displayItem;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return "followers." + name.toLowerCase().replaceAll(" ", "_");
    }

    public @NotNull EntityConfiguration getEntityConfig() {
        return entityConfig;
    }

    public EntityType getEntityType() {
        return entityConfig.getEntityType();
    }

    public SimpleItemStack getDisplayItem() {
        return displayItem;
    }

    public SimpleItemStack getDisplayItemOrSimilar() {
        if (this.displayItem != null && this.displayItem.getType() != Material.AIR) {
            return this.displayItem.clone();
        }

        if (entityConfig instanceof LivingEntityConfiguration livingEntityConfig) {
            SimpleItemStack headItem = livingEntityConfig.getEquipment(EquipmentSlot.HELMET);
            if (headItem != null && headItem.getType() != Material.AIR) {
                return headItem.clone();
            }
        }

        return new SimpleItemStack(EntityTypeUtils.getSpawnEgg(entityConfig.getEntityType()));
    }

    @Deprecated(forRemoval = true)
    public Map<EquipmentSlot, ExtendedSimpleItemStack> getEquipment() {
        return entityConfig instanceof LivingEntityConfiguration livingEntityConfig ? livingEntityConfig.getEquipment() : null;
    }

    @Deprecated(forRemoval = true)
    public @Nullable ExtendedSimpleItemStack getEquipmentSlot(EquipmentSlot slot) {
        return entityConfig instanceof LivingEntityConfiguration livingEntityConfig ? livingEntityConfig.getEquipment(slot) : null;
    }

    @Deprecated(forRemoval = true)
    public SkinData getSkin() {
        return entityConfig instanceof PlayerConfiguration playerConfig ? playerConfig.getSkin() : null;
    }

    @Deprecated(forRemoval = true)
    public boolean isVisible() {
        if (!(entityConfig instanceof LivingEntityConfiguration livingEntityConfig)) {
            return true;
        }

        return !livingEntityConfig.isInvisible();
    }

    @Deprecated(forRemoval = true)
    public double getScale() {
        return entityConfig instanceof LivingEntityConfiguration livingEntityConfig ? livingEntityConfig.getScale() : 1.0;
    }

    public WrapperEntity createEntity() {
        return entityConfig.createEntity();
    }

    /**
     * @param entity A spawned entity
     */
    public void applyAttributes(WrapperEntity entity) {
        entityConfig.applyAttributes(entity);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(FollowerHandler handler) {
        return new Builder(handler);
    }

    public static class Builder {
        private boolean nameLocked = false;
        private String name;
        private EntityType entityType;
        private EntityConfiguration entityConfig;
        private SimpleItemStack displayItem;

        private Builder() {
            this.name = null;
            this.entityType = EntityTypes.ARMOR_STAND;
            this.entityConfig = EntityConfiguration.empty(entityType);
        }

        private Builder(FollowerHandler followerHandler) {
            this.name = followerHandler.getName();
            this.entityType = followerHandler.getEntityType();
            this.entityConfig = followerHandler.getEntityConfig();
            this.displayItem = followerHandler.getDisplayItem();
        }

        public boolean nameLocked() {
            return nameLocked;
        }

        public Builder nameLocked(boolean nameLocked) {
            this.nameLocked = nameLocked;
            return this;
        }

        public String name() {
            return name;
        }

        public Builder name(String name) throws IllegalStateException {
            if (nameLocked) {
                throw new IllegalStateException("Object is name locked, name cannot be changed");
            }

            this.name = name;
            return this;
        }

        public EntityType entityType() {
            return entityType;
        }

        public Builder entityType(EntityType entityType) {
            if (this.entityType == entityType) {
                return this;
            }

            this.entityType = entityType;
            this.entityConfig = EntityConfiguration.empty(entityType);
            return this;
        }

        public EntityConfiguration entityConfig() {
            return entityConfig;
        }

        public SimpleItemStack displayItem() {
            return displayItem;
        }

        public Builder displayItem(SimpleItemStack displayItem) {
            this.displayItem = displayItem;
            return this;
        }

        public FollowerHandler build() {
            return new FollowerHandler(name, entityConfig, displayItem);
        }
    }
}
