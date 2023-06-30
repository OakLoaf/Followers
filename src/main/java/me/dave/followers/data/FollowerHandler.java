package me.dave.followers.data;

import me.dave.followers.exceptions.ObjectNameLockedException;
import me.dave.followers.utils.ItemStackData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FollowerHandler {
    private final String name;
    private final ItemStack head;
    private final ItemStack chest;
    private final ItemStack legs;
    private final ItemStack feet;
    private final ItemStack mainHand;
    private final ItemStack offHand;
    private final boolean isVisible;

    public FollowerHandler(ConfigurationSection configurationSection) {
        this.name = configurationSection.getName();
        this.head = ItemStackData.parse(configurationSection.getConfigurationSection("head"), Material.AIR);
        this.chest = ItemStackData.parse(configurationSection.getConfigurationSection("chest"), Material.AIR);
        this.legs = ItemStackData.parse(configurationSection.getConfigurationSection("legs"), Material.AIR);
        this.feet = ItemStackData.parse(configurationSection.getConfigurationSection("feet"), Material.AIR);
        this.mainHand = ItemStackData.parse(configurationSection.getConfigurationSection("mainHand"), Material.AIR);
        this.offHand = ItemStackData.parse(configurationSection.getConfigurationSection("offHand"), Material.AIR);
        this.isVisible = configurationSection.getBoolean("visible", true);
    }

    private FollowerHandler(String name, ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet, ItemStack mainHand, ItemStack offHand, boolean visible) {
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

    public ItemStack getHead() {
        return head.clone();
    }

    public ItemStack getChest() {
        return chest.clone();
    }

    public ItemStack getLegs() {
        return legs.clone();
    }

    public ItemStack getFeet() {
        return feet.clone();
    }

    public ItemStack getMainHand() {
        return mainHand.clone();
    }

    public ItemStack getOffHand() {
        return offHand.clone();
    }

    public boolean isVisible() {
        return isVisible;
    }


    public static class Builder {
        private boolean nameLocked = false;
        private String name;
        private ItemStack head;
        private ItemStack chest;
        private ItemStack legs;
        private ItemStack feet;
        private ItemStack mainHand;
        private ItemStack offHand;
        private boolean visible;

        public Builder() {
            this.head = new ItemStack(Material.AIR);
            this.chest = new ItemStack(Material.AIR);
            this.legs = new ItemStack(Material.AIR);
            this.feet = new ItemStack(Material.AIR);
            this.mainHand = new ItemStack(Material.AIR);
            this.offHand = new ItemStack(Material.AIR);
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
            if (nameLocked) throw new ObjectNameLockedException("Object is name locked, name cannot be changed");
            this.name = name;
            return this;
        }

        public ItemStack getHead() {
            return head;
        }

        public ItemStack getChest() {
            return chest;
        }

        public ItemStack getLegs() {
            return legs;
        }

        public ItemStack getFeet() {
            return feet;
        }

        public ItemStack getMainHand() {
            return mainHand;
        }

        public ItemStack getOffHand() {
            return offHand;
        }

        public Builder setSlot(EquipmentSlot slot, @NotNull ItemStack item) {
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
