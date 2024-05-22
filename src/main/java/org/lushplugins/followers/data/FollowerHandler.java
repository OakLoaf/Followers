package org.lushplugins.followers.data;

import org.lushplugins.followers.exceptions.ObjectNameLockedException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;

public class FollowerHandler {
    private final String name;
    private final ExtendedSimpleItemStack head;
    private final ExtendedSimpleItemStack chest;
    private final ExtendedSimpleItemStack legs;
    private final ExtendedSimpleItemStack feet;
    private final ExtendedSimpleItemStack mainHand;
    private final ExtendedSimpleItemStack offHand;
    private final boolean isVisible;

    public FollowerHandler(ConfigurationSection configurationSection) {
        this.name = configurationSection.getName();
        this.head = configurationSection.contains("head") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("head")) : new ExtendedSimpleItemStack(Material.AIR);
        this.chest = configurationSection.contains("chest") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("chest")) : new ExtendedSimpleItemStack(Material.AIR);
        this.legs = configurationSection.contains("legs") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("legs")) : new ExtendedSimpleItemStack(Material.AIR);
        this.feet = configurationSection.contains("feet") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("feet")) : new ExtendedSimpleItemStack(Material.AIR);
        this.mainHand = configurationSection.contains("mainHand") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("mainHand")) : new ExtendedSimpleItemStack(Material.AIR);
        this.offHand = configurationSection.contains("offHand") ? new ExtendedSimpleItemStack(configurationSection.getConfigurationSection("offHand")) : new ExtendedSimpleItemStack(Material.AIR);
        this.isVisible = configurationSection.getBoolean("visible", true);
    }

    private FollowerHandler(String name, ExtendedSimpleItemStack head, ExtendedSimpleItemStack chest, ExtendedSimpleItemStack legs, ExtendedSimpleItemStack feet, ExtendedSimpleItemStack mainHand, ExtendedSimpleItemStack offHand, boolean visible) {
        this.name = name;
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.isVisible = visible;
    }

    public String getName() {
        return name;
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


    public static class Builder {
        private boolean nameLocked = false;
        private String name;
        private ExtendedSimpleItemStack head;
        private ExtendedSimpleItemStack chest;
        private ExtendedSimpleItemStack legs;
        private ExtendedSimpleItemStack feet;
        private ExtendedSimpleItemStack mainHand;
        private ExtendedSimpleItemStack offHand;
        private boolean visible;

        public Builder() {
            this.head = new ExtendedSimpleItemStack(Material.AIR);
            this.chest = new ExtendedSimpleItemStack(Material.AIR);
            this.legs = new ExtendedSimpleItemStack(Material.AIR);
            this.feet = new ExtendedSimpleItemStack(Material.AIR);
            this.mainHand = new ExtendedSimpleItemStack(Material.AIR);
            this.offHand = new ExtendedSimpleItemStack(Material.AIR);
            this.visible = true;
        }

        public Builder(FollowerHandler handler) {
            this.name = handler.getName();
            this.head = handler.getHead();
            this.chest = handler.getChest();
            this.legs = handler.getLegs();
            this.feet = handler.getFeet();
            this.mainHand = handler.getMainHand();
            this.offHand = handler.getOffHand();
            this.visible = handler.isVisible;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) throws ObjectNameLockedException {
            if (nameLocked) {
                throw new ObjectNameLockedException("Object is name locked, name cannot be changed");
            }

            this.name = name;
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

        public boolean isNameLocked() {
            return nameLocked;
        }

        public Builder setNameLocked(boolean locked) {
            this.nameLocked = locked;
            return this;
        }

        public FollowerHandler build() {
            return new FollowerHandler(name, head, chest, legs, feet, mainHand, offHand, visible);
        }
    }
}
