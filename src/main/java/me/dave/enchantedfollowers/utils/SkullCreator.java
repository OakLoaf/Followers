package me.dave.enchantedfollowers.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import me.dave.enchantedfollowers.Followers;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkullCreator {

    public CompletableFuture<ItemStack> getPlayerSkull(UUID uuid, Followers plugin) {
        CompletableFuture<ItemStack> futureItemStack = new CompletableFuture<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
                PlayerProfile playerProfile = Bukkit.createProfile(uuid);
                playerProfile.complete();
                skullMeta.setPlayerProfile(playerProfile);
                skullItem.setItemMeta(skullMeta);
                futureItemStack.complete(skullItem);
            }
        }.runTaskAsynchronously(plugin);
        return futureItemStack;
    }

    public ItemStack getCustomSkull(String texture) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
        Set<ProfileProperty> profileProperties = playerProfile.getProperties();
        profileProperties.add(new ProfileProperty("textures", texture));
        playerProfile.setProperties(profileProperties);
        skullMeta.setPlayerProfile(playerProfile);
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
