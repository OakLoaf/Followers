package me.dave.followers.data;

import me.dave.followers.utils.ItemStackData;
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
    private final boolean isVisible;

    public FollowerHandler(ConfigurationSection configurationSection) {
        ItemStackData headData = new ItemStackData(configurationSection.getConfigurationSection("head"), "AIR");
        ItemStackData chestData = new ItemStackData(configurationSection.getConfigurationSection("chest"), "AIR");
        ItemStackData legsData = new ItemStackData(configurationSection.getConfigurationSection("legs"), "AIR");
        ItemStackData feetData = new ItemStackData(configurationSection.getConfigurationSection("feet"), "AIR");
        ItemStackData mainHandData = new ItemStackData(configurationSection.getConfigurationSection("mainHand"), "AIR");
        ItemStackData offHandData = new ItemStackData(configurationSection.getConfigurationSection("offHand"), "AIR");
        Followers plugin = Followers.getInstance();
        head = headData.getItem();
        chest = chestData.getItem();
        legs = legsData.getItem();
        feet = feetData.getItem();
        mainHand = mainHandData.getItem();
        offHand = offHandData.getItem();
        isVisible = configurationSection.getBoolean("visible", true);
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

    public boolean isVisible() {
        return isVisible;
    }
}
