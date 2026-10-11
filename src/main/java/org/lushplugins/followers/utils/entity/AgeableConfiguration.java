package org.lushplugins.followers.utils.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import me.tofaa.entitylib.meta.MetaFields;
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

        EntityType type = entity.getEntityType();
        if (type.isInstanceOf(EntityTypes.ZOMBIE)) {
            entity.setMetadata(MetaFields.Zombie.BABY, baby);
        } else if (type.isInstanceOf(EntityTypes.ARMOR_STAND)) {
            entity.setFlag(MetaFields.ArmorStand.SMALL, baby);
        } else if (type.isInstanceOf(EntityTypes.ABSTRACT_AGEABLE)) {
            entity.setMetadata(MetaFields.AgeableMob.BABY, baby);
        }
    }

    @Override
    public void save(ConfigurationSection config) {
        super.save(config);

        config.set("baby", baby);
    }
}
