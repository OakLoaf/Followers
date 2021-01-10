package org.enchantedskies.esfollowers;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.enchantedskies.esfollowers.commands.FollowerCmd;
import org.enchantedskies.esfollowers.commands.GetHexArmorCmd;
import org.enchantedskies.esfollowers.datamanager.DataManager;
import org.enchantedskies.esfollowers.events.EssentialsEvents;
import org.enchantedskies.esfollowers.events.FollowerGUIEvents;
import org.enchantedskies.esfollowers.events.FollowerUserEvents;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class ESFollowers extends JavaPlugin implements Listener {
    public static DataManager dataManager;
    public static SkullCreator skullCreator = new SkullCreator();
    private final HashMap<UUID, UUID> playerFollowerMap = new HashMap<>();
    private final HashMap<String, ItemStack> followerSkullMap = new HashMap<>();
    private final HashSet<UUID> guiPlayerSet = new HashSet<>();
    private final NamespacedKey followerKey = new NamespacedKey(this, "ESFollower");

    Listener[] listeners = new Listener[] {
        this,
        new FollowerUserEvents(this, followerSkullMap, playerFollowerMap, followerKey),
        new FollowerGUIEvents(this, guiPlayerSet, playerFollowerMap, followerSkullMap, followerKey),
        new FollowerCreator(this, followerSkullMap),
    };

    @Override
    public void onEnable() {
        dataManager = new DataManager(this);

        writeFile();
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        registerEvents(listeners);
        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("Essentials") != null) {
            pluginManager.registerEvents(new EssentialsEvents(this, playerFollowerMap), this);
        } else {
            getLogger().info("Essentials plugin not found. Continuing without Essentials.");
        }
        getCommand("followers").setExecutor(new FollowerCmd(this, guiPlayerSet, followerSkullMap));
        getCommand("gethexarmor").setExecutor(new GetHexArmorCmd());

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
                    if (skullTexture != null) item = skullCreator.getCustomSkull(skullTexture);
                    followerSkullMap.put(followerName, item);
                } else {
                    String skullUUID = configSection.getString("UUID");
                    if (skullUUID == null || skullUUID.equalsIgnoreCase("error")) {
                        followerSkullMap.put(followerName, new ItemStack(Material.PLAYER_HEAD));
                        continue;
                    }
                    skullCreator.getPlayerSkull(UUID.fromString(skullUUID), this).thenAccept(itemStack -> Bukkit.getScheduler().runTask(this, runnable -> { followerSkullMap.put(followerName, itemStack); }));
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
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
