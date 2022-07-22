package me.dave.enchantedfollowers;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import dev.geco.gsit.api.GSitAPI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import me.dave.enchantedfollowers.apis.GSitEvents;
import me.dave.enchantedfollowers.commands.DyeCmd;
import me.dave.enchantedfollowers.commands.FollowerCmd;
import me.dave.enchantedfollowers.commands.GetHexArmorCmd;
import me.dave.enchantedfollowers.datamanager.ConfigManager;
import me.dave.enchantedfollowers.datamanager.DataManager;
import me.dave.enchantedfollowers.datamanager.FollowerManager;
import me.dave.enchantedfollowers.datamanager.Storage;
import me.dave.enchantedfollowers.apis.EssentialsEvents;
import me.dave.enchantedfollowers.events.FollowerGUIEvents;
import me.dave.enchantedfollowers.events.FollowerUserEvents;
import me.dave.enchantedfollowers.utils.SkullCreator;

import java.util.HashSet;
import java.util.UUID;

public final class Followers extends JavaPlugin implements Listener {
    private static Followers plugin;
    public static DataManager dataManager;
    public static ConfigManager configManager;
    public static FollowerManager followerManager;
    public static final SkullCreator skullCreator = new SkullCreator();
    private final HashSet<UUID> guiPlayerSet = new HashSet<>();
    private final NamespacedKey followerKey = new NamespacedKey(this, "Follower");

    public static GSitAPI GAPI;

    public static boolean isGSitEnabled = false;

    private void setThreadIOName() {
        Storage.SERVICE.submit(() -> Thread.currentThread().setName("Followers IO Thread"));
    }

    @Override
    public void onEnable() {
        plugin = this;
        setThreadIOName();
        configManager = new ConfigManager();
        dataManager = new DataManager();
        dataManager.initAsync((successful) -> {
            if (successful) {
                followerManager = new FollowerManager();

                Listener[] listeners = new Listener[] {
                    this,
                    new FollowerUserEvents(),
                    new FollowerGUIEvents(guiPlayerSet),
                    new FollowerCreator()
                };
                registerEvents(listeners);

                PluginManager pluginManager = getServer().getPluginManager();
                if (pluginManager.getPlugin("Essentials") != null) pluginManager.registerEvents(new EssentialsEvents(), this);
                else getLogger().info("Essentials plugin not found. Continuing without Essentials.");
                if (pluginManager.getPlugin("GSit") != null) {
                    pluginManager.registerEvents(new GSitEvents(), this);
                    GAPI = new GSitAPI();
                    isGSitEnabled = true;
                }
                else getLogger().info("GSit plugin not found. Continuing without GSit.");

                getCommand("followers").setExecutor(new FollowerCmd(guiPlayerSet));
                getCommand("gethexarmor").setExecutor(new GetHexArmorCmd());
                getCommand("dye").setExecutor(new DyeCmd());

                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        for (Entity entity : chunk.getEntities()) {
                            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
                        }
                    }
                }
            } else {
                Bukkit.getLogger().severe("Could not initialise the data. Aborting further plugin setup.");
            }
        });
    }

    @Override
    public void onDisable() {
        Storage.SERVICE.shutdownNow();
    }

    @EventHandler
    public void onEntityLoad(EntityAddToWorldEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Entity entity = event.getEntity();
            if (entity.getType() != EntityType.ARMOR_STAND) return;
            if (dataManager.getActiveArmorStandsSet().contains(entity.getUniqueId())) return;
            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
        }, 1);
    }

    public static Followers getInstance() {
        return plugin;
    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
