package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class FollowerHandler {
    private ItemStack head;
    private ItemStack chest;
    private ItemStack legs;
    private ItemStack feet;
    private ItemStack mainHand;
    private ItemStack offHand;

    public FollowerHandler(ConfigurationSection configurationSection) {
        ItemStackData headData = new ItemStackData(configurationSection.getConfigurationSection("Head"), "AIR");
        ItemStackData chestData = new ItemStackData(configurationSection.getConfigurationSection("Chest"), "AIR");
        ItemStackData legsData = new ItemStackData(configurationSection.getConfigurationSection("Legs"), "AIR");
        ItemStackData feetData = new ItemStackData(configurationSection.getConfigurationSection("Feet"), "AIR");
        ItemStackData mainHandData = new ItemStackData(configurationSection.getConfigurationSection("MainHand"), "AIR");
        ItemStackData offHandData = new ItemStackData(configurationSection.getConfigurationSection("OffHand"), "AIR");
        head = headData.getItem();
        chest = chestData.getItem();
        legs = legsData.getItem();
        feet = feetData.getItem();
        mainHand = mainHandData.getItem();
        offHand = offHandData.getItem();
    }

    public FollowerHandler() {
        head = new ItemStack(Material.AIR);
        chest = new ItemStack(Material.AIR);
        legs = new ItemStack(Material.AIR);
        feet = new ItemStack(Material.AIR);
        mainHand = new ItemStack(Material.AIR);
        offHand = new ItemStack(Material.AIR);
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

}
