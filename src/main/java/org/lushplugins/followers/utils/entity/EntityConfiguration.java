package org.lushplugins.followers.utils.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.mobs.monster.zombie.ZombieMeta;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.meta.types.AgeableMeta;
import me.tofaa.entitylib.meta.types.LivingEntityMeta;
import me.tofaa.entitylib.meta.types.PlayerMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.gui.button.BooleanButton;
import org.lushplugins.followers.utils.ClassUtils;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;

import java.util.List;
import java.util.UUID;

// TODO: Consider moving to ConfigurationData
public class EntityConfiguration {
    private final EntityType entityType;
    private boolean invisible;

    @SuppressWarnings("unused")
    protected EntityConfiguration(EntityType entityType, ConfigurationSection config) {
        this.entityType = entityType;
        this.invisible = !config.getBoolean("visible", true);
    }

    protected EntityConfiguration(EntityType entityType) {
        this.entityType = entityType;
        this.invisible = false;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public List<ItemButton> getGuiButtons(Gui gui) {
        return List.of(
            new BooleanButton(
                invisible,
                () -> Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "visibility-button.invisible", Material.GLASS).asItemStack(),
                () -> Followers.getInstance().getConfigManager().getGuiItem("builder-gui", "visibility-button.visible", Material.WHITE_STAINED_GLASS).asItemStack(),
                (value) -> this.invisible = value
            )
        );
    }

    public WrapperEntity createEntity() {
        UUID uuid = EntityLib.getPlatform().getEntityUuidProvider().provide(entityType);
        int entityId = EntityLib.getPlatform().getEntityIdProvider().provide(uuid, entityType);

        return createEntity(entityId, uuid);
    }

    public WrapperEntity createEntity(int entityId, UUID uuid) {
        return createEntity(entityId, uuid, EntityMeta.createMeta(entityId, entityType));
    }

    public WrapperEntity createEntity(int entityId, UUID uuid, EntityMeta meta) {
        return new WrapperEntity(entityId, uuid, entityType, meta);
    }

    public void applyAttributes(WrapperEntity entity) {
        entity.getEntityMeta().setInvisible(invisible);
    }

    public void save(ConfigurationSection config) {
        config.set("entityType", entityType.getName().toString());
        config.set("visible", !invisible);
    }

    public static EntityConfiguration from(ConfigurationSection config) {
        NamespacedKey entityTypeKey = NamespacedKey.fromString(config.getString("entityType", "armor_stand"));
        if (entityTypeKey == null) {
            throw new IllegalArgumentException("No entity type defined for pet '" + config.getCurrentPath() + "'");
        }

        EntityType entityType = EntityTypes.getByName(entityTypeKey.toString());
        if (entityType == null) {
            throw new IllegalArgumentException("Invalid entity type defined for pet '" + config.getCurrentPath() + "'");
        }

        Class<?> metaClass = EntityMeta.getMetaClass(entityType);
        if (ClassUtils.areAnyAssignable(metaClass, PlayerMeta.class)) {
            return new PlayerConfiguration(entityType, config);
        } else if (ClassUtils.areAnyAssignable(metaClass, AgeableMeta.class, ArmorStandMeta.class, ZombieMeta.class)) {
            return new AgeableConfiguration(entityType, config);
        } else if (ClassUtils.areAnyAssignable(metaClass, LivingEntityMeta.class)) {
            return new LivingEntityConfiguration(entityType, config);
        } else {
            return new EntityConfiguration(entityType, config);
        }
    }

    public static EntityConfiguration empty(EntityType entityType) {
        Class<?> metaClass = EntityMeta.getMetaClass(entityType);
        if (ClassUtils.areAnyAssignable(metaClass, PlayerMeta.class)) {
            return new PlayerConfiguration(entityType);
        } else if (ClassUtils.areAnyAssignable(metaClass, AgeableMeta.class, ArmorStandMeta.class, ZombieMeta.class)) {
            return new AgeableConfiguration(entityType);
        } else if (ClassUtils.areAnyAssignable(metaClass, LivingEntityMeta.class)) {
            return new LivingEntityConfiguration(entityType);
        } else {
            return new EntityConfiguration(entityType);
        }
    }
}
