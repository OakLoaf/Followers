package me.dave.followers.utils.skullcreator;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public interface SkullCreator {
    ItemStack getCustomSkull(String texture);

    ItemStack getPlayerSkull(UUID uuid);

    void mutateItemMeta(SkullMeta meta, String b64);

    String getB64(ItemStack itemStack);

    String getTexture(Player player);
}
