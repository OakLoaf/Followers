package org.lushplugins.followers.utils.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.mobs.monster.zombie.ZombieMeta;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.meta.types.AgeableMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.followers.gui.button.BooleanButton;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.utils.DisplayItemStack;

import java.util.ArrayList;
import java.util.List;

public class AgeableConfiguration extends LivingEntityConfiguration {
    private boolean baby;

    protected AgeableConfiguration(EntityType entityType, ConfigurationSection config) {
        super(entityType, config);

        this.baby = config.getBoolean("baby", entityType == EntityTypes.ARMOR_STAND);
    }

    protected AgeableConfiguration(EntityType entityType) {
        super(entityType);

        this.baby = entityType == EntityTypes.ARMOR_STAND;
    }

    public boolean isBaby() {
        return baby;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    @Override
    public List<ItemButton> getGuiButtons(Gui gui) {
        List<ItemButton> buttons = new ArrayList<>(super.getGuiButtons(gui));

        buttons.add(
            new BooleanButton(
                baby,
                () -> DisplayItemStack.builder(Material.LIME_DYE)
                    .setDisplayName("&#ffde8aBaby: &fTrue")
                    .build()
                    .asItemStack(),
                () -> DisplayItemStack.builder(Material.RED_DYE)
                    .setDisplayName("&#ffde8aBaby: &fFalse")
                    .build()
                    .asItemStack(),
                (value) -> this.baby = value)
        );

        return buttons;
    }

    @Override
    public void applyAttributes(WrapperEntity entity) {
        super.applyAttributes(entity);

        EntityMeta meta = entity.getEntityMeta();
        if (meta instanceof ZombieMeta zombieMeta) {
            zombieMeta.setBaby(baby);
        } else if (meta instanceof ArmorStandMeta armorStandMeta) {
            armorStandMeta.setSmall(baby);
        } else if (meta instanceof AgeableMeta ageableMeta) {
            ageableMeta.setBaby(baby);
        }
    }

    @Override
    public void save(ConfigurationSection config) {
        super.save(config);

        config.set("baby", baby);
    }
}
