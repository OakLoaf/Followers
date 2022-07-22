package me.dave.followers.datamanager;

import org.bukkit.configuration.ConfigurationSection;
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

    public FollowerHandler(ConfigurationSection configurationSection) {
        ItemStackData headData = new ItemStackData(configurationSection.getConfigurationSection("Head"), "AIR");
        ItemStackData chestData = new ItemStackData(configurationSection.getConfigurationSection("Chest"), "AIR");
        ItemStackData legsData = new ItemStackData(configurationSection.getConfigurationSection("Legs"), "AIR");
        ItemStackData feetData = new ItemStackData(configurationSection.getConfigurationSection("Feet"), "AIR");
        ItemStackData mainHandData = new ItemStackData(configurationSection.getConfigurationSection("MainHand"), "AIR");
        ItemStackData offHandData = new ItemStackData(configurationSection.getConfigurationSection("OffHand"), "AIR");
        Followers plugin = Followers.getInstance();
        head = headData.getItem();
        chest = chestData.getItem();
        legs = legsData.getItem();
        feet = feetData.getItem();
        mainHand = mainHandData.getItem();
        offHand = offHandData.getItem();
        new BukkitRunnable() {
            @Override
            public void run() {
                head = headData.getItem();
                chest = chestData.getItem();
                legs = legsData.getItem();
                feet = feetData.getItem();
                mainHand = mainHandData.getItem();
                offHand = offHandData.getItem();
            }
        }.runTaskLater(plugin, 100L);
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
