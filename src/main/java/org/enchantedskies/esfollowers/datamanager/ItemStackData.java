package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.enchantedskies.esfollowers.ESFollowers;

import java.util.UUID;

public class ItemStackData {
    private final ESFollowers plugin = ESFollowers.getInstance();
    private ItemStack item;
    private String colour;
    private boolean isEnchanted;

    public ItemStackData(ConfigurationSection configurationSection) {
        init(configurationSection, "STONE");
    }

    public ItemStackData(ConfigurationSection configurationSection, String material) {
        init(configurationSection, material);
    }

    public void init(ConfigurationSection configurationSection, String aMaterial) {
        Material material = Material.valueOf(configurationSection.getString("Material", aMaterial).toUpperCase());
        colour = configurationSection.getString("colour", "A06540");
        isEnchanted = Boolean.parseBoolean(configurationSection.getString("enchanted", "false"));
        item = new ItemStack(material);
        if (material == Material.PLAYER_HEAD) {
            String skullType = configurationSection.getString("SkullType", "");
            if (skullType.equalsIgnoreCase("custom")) {
                String skullTexture = configurationSection.getString("Texture");
                if (skullTexture != null) item = ESFollowers.skullCreator.getCustomSkull(skullTexture);
            } else {
                String skullUUID = configurationSection.getString("UUID");
                if (skullUUID == null || skullUUID.equalsIgnoreCase("error")) {
                    item = new ItemStack(Material.PLAYER_HEAD);
                }
                ESFollowers.skullCreator.getPlayerSkull(UUID.fromString(skullUUID), plugin).thenAccept(itemStack -> Bukkit.getScheduler().runTask(plugin, runnable -> { item = itemStack; }));
            }
        }
        else if (item.getItemMeta() instanceof LeatherArmorMeta) {
            item = getColoredArmour(material, colour);
            return;
        }
        if (isEnchanted) {
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            item.setItemMeta(itemMeta);
        }
    }

    private ItemStack getColoredArmour(Material material, String hexColour) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof LeatherArmorMeta)) return item;
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) item.getItemMeta();
        int red = Integer.valueOf(hexColour.substring(0, 2), 16);
        int green = Integer.valueOf(hexColour.substring(2, 4), 16);
        int blue = Integer.valueOf(hexColour.substring(4, 6), 16);
        armorMeta.setColor(Color.fromRGB(red, green, blue));
        item.setItemMeta(armorMeta);
        return item;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getColour() {
        return colour;
    }

    public boolean isEnchanted() {
        return isEnchanted;
    }
}