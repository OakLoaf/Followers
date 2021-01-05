package org.enchantedskies.esfollowers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FollowerGUI {
    private final Inventory inventory;
    private final ESFollowers plugin;
    private final FileConfiguration config;
    private final HashSet<UUID> playerSet;

    public FollowerGUI(ESFollowers instance, HashSet<UUID> playerSet) {
        plugin = instance;
        config = plugin.getConfig();
        this.playerSet = playerSet;
        inventory = Bukkit.createInventory(null, 54, "Followers");
        ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta emptyMeta = empty.getItemMeta();
        emptyMeta.setDisplayName("§r");
        empty.setItemMeta(emptyMeta);
        for (int i = 0; i < 18; i++) {
            if (i <= 8) inventory.setItem(i, empty);
            else inventory.setItem(i + 36, empty);
        }
        int i = 8;
        for (String followerName : config.getKeys(false)) {
            i += 1;
            ConfigurationSection configSection = config.getConfigurationSection(followerName + ".Head");
            if (configSection == null) continue;
            String materialStr = configSection.getString("Material", "");
            Material material = Material.getMaterial(materialStr.toUpperCase());
            if (material == null) continue;
            ItemStack item = new ItemStack(material);
            if (material == Material.PLAYER_HEAD) {
                String skullType = configSection.getString("SkullType");
                if (skullType.equalsIgnoreCase("custom")) {
                    String skullTexture = configSection.getString("Texture");
                    if (skullTexture != null) item = getCustomSkull(skullTexture);
                } else {
                    String skullUUID = configSection.getString("UUID");
                    int finalI = i;
                    getPlayerSkull(UUID.fromString(skullUUID)).thenAccept(itemStack -> Bukkit.getScheduler().runTask(plugin, runnable -> {
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName("§e" + followerName);
                        itemStack.setItemMeta(itemMeta);
                        inventory.setItem(finalI, itemStack);
                    }));
                    continue;
                }
            }
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName("§e" + followerName);
            item.setItemMeta(itemMeta);
            inventory.setItem(i, item);
        }
    }

    public void openInventory(Player player) {
        playerSet.add(player.getUniqueId());
        player.openInventory(inventory);
    }

    private CompletableFuture<ItemStack> getPlayerSkull(UUID uuid) {
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

    private ItemStack getCustomSkull(String texture) {
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