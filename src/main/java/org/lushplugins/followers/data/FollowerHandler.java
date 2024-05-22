package org.lushplugins.followers.data;

import org.lushplugins.followers.exceptions.ObjectNameLockedException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.utils.SimpleItemStack;

public class FollowerHandler {
    private final String name;
    private final SimpleItemStack head;
    private final SimpleItemStack chest;
    private final SimpleItemStack legs;
    private final SimpleItemStack feet;
    private final SimpleItemStack mainHand;
    private final SimpleItemStack offHand;
    private final boolean isVisible;

    public FollowerHandler(ConfigurationSection configurationSection) {
        this.name = configurationSection.getName();
        this.head = configurationSection.contains("head") ? new SimpleItemStack(configurationSection.getConfigurationSection("head")) : new SimpleItemStack(Material.AIR);
        this.chest = configurationSection.contains("chest") ? new SimpleItemStack(configurationSection.getConfigurationSection("chest")) : new SimpleItemStack(Material.AIR);
        this.legs = configurationSection.contains("legs") ? new SimpleItemStack(configurationSection.getConfigurationSection("legs")) : new SimpleItemStack(Material.AIR);
        this.feet = configurationSection.contains("feet") ? new SimpleItemStack(configurationSection.getConfigurationSection("feet")) : new SimpleItemStack(Material.AIR);
        this.mainHand = configurationSection.contains("mainHand") ? new SimpleItemStack(configurationSection.getConfigurationSection("mainHand")) : new SimpleItemStack(Material.AIR);
        this.offHand = configurationSection.contains("offHand") ? new SimpleItemStack(configurationSection.getConfigurationSection("offHand")) : new SimpleItemStack(Material.AIR);
        this.isVisible = configurationSection.getBoolean("visible", true);
    }

    private FollowerHandler(String name, SimpleItemStack head, SimpleItemStack chest, SimpleItemStack legs, SimpleItemStack feet, SimpleItemStack mainHand, SimpleItemStack offHand, boolean visible) {
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

    public SimpleItemStack getHead() {
        return head.clone();
    }

    public SimpleItemStack getChest() {
        return chest.clone();
    }

    public SimpleItemStack getLegs() {
        return legs.clone();
    }

    public SimpleItemStack getFeet() {
        return feet.clone();
    }

    public SimpleItemStack getMainHand() {
        return mainHand.clone();
    }

    public SimpleItemStack getOffHand() {
        return offHand.clone();
    }

    public boolean isVisible() {
        return isVisible;
    }


    public static class Builder {
        private boolean nameLocked = false;
        private String name;
        private SimpleItemStack head;
        private SimpleItemStack chest;
        private SimpleItemStack legs;
        private SimpleItemStack feet;
        private SimpleItemStack mainHand;
        private SimpleItemStack offHand;
        private boolean visible;

        public Builder() {
            this.head = new SimpleItemStack(Material.AIR);
            this.chest = new SimpleItemStack(Material.AIR);
            this.legs = new SimpleItemStack(Material.AIR);
            this.feet = new SimpleItemStack(Material.AIR);
            this.mainHand = new SimpleItemStack(Material.AIR);
            this.offHand = new SimpleItemStack(Material.AIR);
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

        public SimpleItemStack getSlot(@NotNull EquipmentSlot slot) {
            SimpleItemStack output = null;
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

        public Builder setSlot(EquipmentSlot slot, @Nullable SimpleItemStack item) {
            if (item == null) {
                item = new SimpleItemStack(Material.AIR);
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
