package me.dave.followers.data;

import me.dave.followers.utils.ItemStackData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import me.dave.followers.Followers;

public class FollowerHandler {
    private ItemStack head;
    private ItemStack chest;
    private ItemStack legs;
    private ItemStack feet;
    private ItemStack mainHand;
    private ItemStack offHand;
    private final boolean isVisible;

    public FollowerHandler(ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet, ItemStack mainHand, ItemStack offHand, boolean visible) {
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.isVisible = visible;
    }

    public FollowerHandler(ConfigurationSection configurationSection) {
        head = ItemStackData.parse(configurationSection.getConfigurationSection("head"), Material.AIR);
        chest = ItemStackData.parse(configurationSection.getConfigurationSection("chest"), Material.AIR);
        legs = ItemStackData.parse(configurationSection.getConfigurationSection("legs"), Material.AIR);
        feet = ItemStackData.parse(configurationSection.getConfigurationSection("feet"), Material.AIR);
        mainHand = ItemStackData.parse(configurationSection.getConfigurationSection("mainHand"), Material.AIR);
        offHand = ItemStackData.parse(configurationSection.getConfigurationSection("offHand"), Material.AIR);
        isVisible = configurationSection.getBoolean("visible", true);
        new BukkitRunnable() {
            @Override
            public void run() {
                head = ItemStackData.parse(configurationSection.getConfigurationSection("head"), Material.AIR);
                chest = ItemStackData.parse(configurationSection.getConfigurationSection("chest"), Material.AIR);
                legs = ItemStackData.parse(configurationSection.getConfigurationSection("legs"), Material.AIR);
                feet = ItemStackData.parse(configurationSection.getConfigurationSection("feet"), Material.AIR);
                mainHand = ItemStackData.parse(configurationSection.getConfigurationSection("mainHand"), Material.AIR);
                offHand = ItemStackData.parse(configurationSection.getConfigurationSection("offHand"), Material.AIR);
            }
        }.runTaskLater(Followers.getInstance(), 100L);
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

    public boolean isVisible() {
        return isVisible;
    }


    protected static class Builder {
        private ItemStack head;
        private ItemStack chest;
        private ItemStack legs;
        private ItemStack feet;
        private ItemStack mainHand;
        private ItemStack offHand;
        private boolean visible;

        public Builder(FollowerHandler handler) {
            this.head = handler.getHead();
            this.chest = handler.getChest();
            this.legs = handler.getLegs();
            this.feet = handler.getFeet();
            this.mainHand = handler.getMainHand();
            this.offHand = handler.getOffHand();
            this.visible = handler.isVisible;
        }

        public Builder setSlot(EquipmentSlot slot, ItemStack item) {
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

        public Builder setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public FollowerHandler build() {
            return new FollowerHandler(head, chest, legs, feet, mainHand, offHand, visible);
        }
    }
}
