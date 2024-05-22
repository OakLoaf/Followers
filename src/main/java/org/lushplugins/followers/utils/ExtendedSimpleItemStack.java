package org.lushplugins.followers.utils;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushlib.utils.RegistryUtils;
import org.lushplugins.lushlib.utils.SkullCreator;

import java.util.UUID;

public class ExtendedSimpleItemStack extends org.lushplugins.lushlib.utils.SimpleItemStack implements Cloneable {
    private Color dyeColor;
    private ArmorTrim armorTrim;

    public ExtendedSimpleItemStack(Material material) {
        super(material);
    }

    public ExtendedSimpleItemStack(ItemStack itemStack) {
        super(itemStack);

        if (itemStack.getItemMeta() instanceof ArmorMeta armorMeta) {
            if (armorMeta.hasTrim()) {
                armorTrim = armorMeta.getTrim();
            }

            if (armorMeta instanceof LeatherArmorMeta leatherArmorMeta) {
                dyeColor = leatherArmorMeta.getColor();
            }
        }
    }

    public ExtendedSimpleItemStack(ConfigurationSection configurationSection) {
        super(configurationSection);

        if (configurationSection.contains("name")) {
            this.setDisplayName(configurationSection.getString("name"));
        }
        if (configurationSection.contains("customModelData")) {
            this.setCustomModelData(configurationSection.getInt("customModelData"));
        }
        if (configurationSection.contains("skullType") && configurationSection.contains("texture")) {
            if (configurationSection.getString("skullType").equals("custom")) {
                this.setSkullTexture(configurationSection.getString("texture"));
            } else {
                UUID uuid = UUID.fromString(configurationSection.getString("uuid"));
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                this.setSkullTexture(SkullCreator.getTexture(player));
            }
        }
        if (configurationSection.contains("color")) {
            String colorRaw = configurationSection.getString("color");
            int red = Integer.valueOf(colorRaw.substring(0, 2), 16);
            int green = Integer.valueOf(colorRaw.substring(2, 4), 16);
            int blue = Integer.valueOf(colorRaw.substring(4, 6), 16);
            dyeColor = Color.fromRGB(red, green, blue);
        }
        if (Bukkit.getVersion().contains("1.20") && configurationSection.contains("trim")) {
            TrimMaterial trimMaterial = RegistryUtils.fromString(Registry.TRIM_MATERIAL, configurationSection.getString("trim.material"));
            TrimPattern trimPattern = RegistryUtils.fromString(Registry.TRIM_PATTERN, configurationSection.getString("trim.pattern"));
            if (trimMaterial != null && trimPattern != null) {
                armorTrim = new ArmorTrim(trimMaterial, trimPattern);
            }
        }
    }

    public Color getDyeColor() {
        return dyeColor;
    }

    public void setDyeColor(Color dyeColor) {
        this.dyeColor = dyeColor;
    }

    public ArmorTrim getArmorTrim() {
        return armorTrim;
    }

    public void setArmorTrim(ArmorTrim armorTrim) {
        this.armorTrim = armorTrim;
    }

    @Override
    public ItemStack asItemStack(@Nullable Player player, boolean parseColors) {
        ItemStack itemStack = super.asItemStack(player, parseColors);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta instanceof ArmorMeta armorMeta) {
                armorMeta.setTrim(armorTrim);

                if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta) {
                    leatherArmorMeta.setColor(dyeColor);
                }
            }
        }

        return itemStack;
    }

    public void save(ConfigurationSection parentSection, String sectionName) {
        if (this.getType() != null && !this.getType().equals(Material.AIR)) {
            save(parentSection.createSection(sectionName));
        }
    }

    @Override
    public void save(ConfigurationSection configurationSection) {
        if (!configurationSection.contains("type") || configurationSection.getString("type").equalsIgnoreCase("air")) {
            return;
        }

        super.save(configurationSection);
        if (dyeColor != null) {
            configurationSection.set("color", String.format("%02x%02x%02x", dyeColor.getRed(), dyeColor.getGreen(), dyeColor.getBlue()));
        }
        if (armorTrim != null) {
            configurationSection.set("trim.material", armorTrim.getMaterial().getKey().toString());
            configurationSection.set("trim.pattern", armorTrim.getPattern().getKey().toString());
        }
    }

    @Override
    public ExtendedSimpleItemStack clone() {
        ExtendedSimpleItemStack clone = (ExtendedSimpleItemStack) super.clone();
        clone.setDyeColor(dyeColor);
        clone.setArmorTrim(armorTrim);

        return clone;
    }
}
