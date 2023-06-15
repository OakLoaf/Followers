package me.dave.followers.utils;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import me.dave.followers.Followers;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemStackData {

    public static ItemStack parse(ConfigurationSection configurationSection, Material material) {
        if (configurationSection == null || material == Material.AIR) return new ItemStack(Material.AIR);

        ItemStack item = new ItemStack(material);
        String colour = configurationSection.getString("color", "A06540");
        boolean isEnchanted = Boolean.parseBoolean(configurationSection.getString("enchanted", "false"));

        if (material == Material.PLAYER_HEAD) {
            String skullType = configurationSection.getString("skullType", "");
            if (skullType.equalsIgnoreCase("custom")) {
                String skullTexture = configurationSection.getString("texture");
                if (skullTexture != null) item = Followers.skullCreator.getCustomSkull(skullTexture);
            } else {
                String skullUUID = configurationSection.getString("uuid");
                if (skullUUID == null || skullUUID.equalsIgnoreCase("error")) item = new ItemStack(Material.PLAYER_HEAD);
                else item = Followers.skullCreator.getPlayerSkull(UUID.fromString(skullUUID));
            }
        }
        else if (item.getItemMeta() instanceof LeatherArmorMeta) {
            item = getColoredArmour(material, colour);
        }

        ItemMeta itemMeta = item.getItemMeta();
        String displayName = configurationSection.getString("name");
        if (displayName != null) itemMeta.setDisplayName(ChatColorHandler.translateAlternateColorCodes(displayName));

        List<String> loreList = new ArrayList<>();
        configurationSection.getStringList("lore").forEach((loreLine) -> loreList.add(ChatColorHandler.translateAlternateColorCodes(loreLine)));
        itemMeta.setLore(loreList);

        int customModelData = configurationSection.getInt("customModelData", -1);
        if (customModelData >= 0) itemMeta.setCustomModelData(customModelData);

        if (isEnchanted) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    public static void save(ItemStack item, ConfigurationSection configurationSection) {
        Material material = item.getType();
        if (material == Material.AIR) return;
        configurationSection.set("material", material.toString().toLowerCase());

        if (item.getEnchantments().size() >= 1) configurationSection.set("enchanted", "True");

        if (material == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            OfflinePlayer skullOwner = skullMeta.getOwningPlayer();
            if (skullOwner == null) {
                configurationSection.set("skullType", "custom");
                String textureStr = Followers.skullCreator.getB64(item);
                configurationSection.set("texture", textureStr);
                return;
            }
            configurationSection.set("skullType", "default");
            UUID skullUUID = skullOwner.getUniqueId();
            configurationSection.set("uuid", skullUUID.toString());
        } else if (item.getItemMeta() instanceof LeatherArmorMeta armorMeta) {
            Color armorColor = armorMeta.getColor();
            configurationSection.set("color", String.format("%02x%02x%02x", armorColor.getRed(), armorColor.getGreen(), armorColor.getBlue()));
        }
    }

    private static ItemStack getColoredArmour(Material material, String hexColour) {
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
}