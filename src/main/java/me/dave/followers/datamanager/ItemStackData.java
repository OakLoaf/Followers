package me.dave.followers.datamanager;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import me.dave.followers.Followers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemStackData {
    private ItemStack item;

    public ItemStackData(ConfigurationSection configurationSection, String material) {
        init(configurationSection, material);
    }

    public void init(ConfigurationSection configurationSection, String aMaterial) {
        if (configurationSection == null) {
            item = new ItemStack(Material.AIR);
            return;
        }
        Material material = Material.valueOf(configurationSection.getString("material", aMaterial).toUpperCase());
        String colour = configurationSection.getString("color", "A06540");
        boolean isEnchanted = Boolean.parseBoolean(configurationSection.getString("enchanted", "false"));
        item = new ItemStack(material);

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

        if (isEnchanted) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(itemMeta);
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
}