package org.enchantedskies.esfollowers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.enchantedskies.esfollowers.commands.Follower;
import org.enchantedskies.esfollowers.datamanager.DataManager;
import org.enchantedskies.esfollowers.events.FollowerEvents;
import org.enchantedskies.esfollowers.events.FollowerGUIEvents;
import org.enchantedskies.esfollowers.events.FollowerUserEvents;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ESFollowers extends JavaPlugin implements Listener {
    public static NamespacedKey followerKey;
    public static DataManager dataManager   ;
    private final HashMap<UUID, UUID> playerFollowerMap = new HashMap<>();
    private final HashMap<String, ItemStack> followerSkullMap = new HashMap<>();
    private final HashSet<UUID> guiPlayerSet = new HashSet<>();
    Listener[] listeners = new Listener[] {
        this,
        new FollowerUserEvents(this, followerSkullMap, playerFollowerMap),
        new FollowerGUIEvents(this, guiPlayerSet, playerFollowerMap, followerSkullMap),
        new FollowerEvents(this, playerFollowerMap),
        new FollowerCreator(this, followerSkullMap)
    };

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);
        followerKey = new NamespacedKey(this, "ESFollower");

        writeFile();
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        registerEvents(listeners);
        getCommand("follower").setExecutor(new Follower(this, guiPlayerSet, followerSkullMap));

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
                }
            }
        }

        for (String followerName : config.getKeys(false)) {
            ConfigurationSection configSection = config.getConfigurationSection(followerName + ".Head");
            if (configSection == null) continue;
            String materialStr = configSection.getString("Material", "");
            Material material = Material.getMaterial(materialStr.toUpperCase());
            if (material == null) continue;
            ItemStack item = new ItemStack(material);
            if (material == Material.PLAYER_HEAD) {
                String skullType = configSection.getString("SkullType", "");
                if (skullType.equalsIgnoreCase("custom")) {
                    String skullTexture = configSection.getString("Texture");
                    if (skullTexture != null) item = getCustomSkull(skullTexture);
                    followerSkullMap.put(followerName, item);
                } else {
                    String skullUUID = configSection.getString("UUID");
                    if (skullUUID == null || skullUUID.equalsIgnoreCase("error")) {
                        followerSkullMap.put(followerName, new ItemStack(Material.PLAYER_HEAD));
                        continue;
                    }
                    getPlayerSkull(UUID.fromString(skullUUID)).thenAccept(itemStack -> Bukkit.getScheduler().runTask(this, runnable -> { followerSkullMap.put(followerName, itemStack); }));
                }
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
        }
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
        }.runTaskAsynchronously(this);
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

    public void writeFile() {
        File dataFile = new File(this.getDataFolder(),"data.yml");
        try {
            if (dataFile.createNewFile()) System.out.println("File Created: data.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
