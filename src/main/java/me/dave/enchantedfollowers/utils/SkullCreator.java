package me.dave.enchantedfollowers.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import me.dave.enchantedfollowers.Followers;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.UUID;

public class SkullCreator {
    private Field field_SkullMeta_profile;

    public ItemStack getPlayerSkull(UUID uuid) {
        return dev.dbassett.skullcreator.SkullCreator.itemFromUuid(uuid);
    }

    public ItemStack getCustomSkull(String texture) {
        return dev.dbassett.skullcreator.SkullCreator.itemFromBase64(texture);
    }

    public String getB64(ItemStack itemStack) {
        try {
            if (itemStack.isSimilar(new ItemStack(Material.PLAYER_HEAD)) && itemStack.hasItemMeta()) {
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                if (!skullMeta.hasOwner()) {
                    if (field_SkullMeta_profile == null) {
                        field_SkullMeta_profile = skullMeta.getClass().getDeclaredField("profile");
                        field_SkullMeta_profile.setAccessible(true);
                    }
                    GameProfile gameProfile = (GameProfile) field_SkullMeta_profile.get(skullMeta);
                    Iterator<Property> iterator = gameProfile.getProperties().get("textures").iterator();
                    if (iterator.hasNext()) {
                        Property property = iterator.next();
                        return property.getValue();
                    }
                }
            }
            return "";
        } catch (Exception exception) {
            return "";
        }
    }
}
