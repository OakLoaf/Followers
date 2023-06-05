package me.dave.followers;

import me.dave.followers.apis.PlaceholderAPIHook;
import me.dave.followers.apis.SimpleSitHook;
import me.dave.followers.events.WorldEvents;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import me.dave.followers.apis.GSitHook;
import me.dave.followers.commands.DyeCmd;
import me.dave.followers.commands.FollowerCmd;
import me.dave.followers.commands.GetHexArmorCmd;
import me.dave.followers.data.ConfigManager;
import me.dave.followers.data.DataManager;
import me.dave.followers.data.FollowerManager;
import me.dave.followers.data.Storage;
import me.dave.followers.apis.EssentialsHook;
import me.dave.followers.events.FollowerGUIEvents;
import me.dave.followers.events.FollowerUserEvents;
import me.dave.followers.utils.SkullCreator;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public final class Followers extends JavaPlugin {
    private static Followers plugin;
    public static DataManager dataManager;
    public static ConfigManager configManager;
    public static FollowerManager followerManager;
    public static final SkullCreator skullCreator = new SkullCreator();
    private final HashSet<UUID> guiPlayerSet = new HashSet<>();
    private final NamespacedKey followerKey = new NamespacedKey(this, "Follower");
    private static int tickCount;

    private static boolean hasGSit = false;
    private static boolean hasFloodgate = false;

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

                Listener[] listeners = new Listener[]{
                        new FollowerUserEvents(),
                        new FollowerGUIEvents(guiPlayerSet),
                        new WorldEvents(),
                        new FollowerCreator()
                };
                registerEvents(listeners);

                PluginManager pluginManager = getServer().getPluginManager();
                if (pluginManager.getPlugin("Essentials") != null) pluginManager.registerEvents(new EssentialsHook(), this);
                else getLogger().info("Essentials plugin not found. Continuing without Essentials.");

                if (pluginManager.getPlugin("GSit") != null) {
                    pluginManager.registerEvents(new GSitHook(), this);
                    hasGSit = true;
                    getLogger().info("Found plugin \"GSIT\". GSit support enabled.");
                }

                if (pluginManager.getPlugin("SimpleSit") != null) {
                    pluginManager.registerEvents(new SimpleSitHook(), this);
                    hasGSit = true;
                    getLogger().info("Found plugin \"SimpleSit\". GSit support enabled.");
                }

                if (pluginManager.getPlugin("PlaceholderAPI") != null) new PlaceholderAPIHook().register();
                else getLogger().info("PlaceholderAPI plugin not found. Continuing without PlaceholderAPI.");

                if (this.getServer().getPluginManager().getPlugin("Floodgate") != null) hasFloodgate = true;
                else getLogger().info("Floodgate plugin not found. Continuing without Floodgate.");

                getCommand("followers").setExecutor(new FollowerCmd(guiPlayerSet));
                getCommand("gethexarmor").setExecutor(new GetHexArmorCmd());
                getCommand("dye").setExecutor(new DyeCmd());

                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        for (Entity entity : chunk.getEntities()) {
                            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING))
                                entity.remove();
                        }
                    }
                }
            } else {
                Bukkit.getLogger().severe("Could not initialise the data. Aborting further plugin setup.");
            }
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                tickCount += 1;
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    @Override
    public void onDisable() {
        Storage.SERVICE.shutdownNow();
    }

    public static Followers getInstance() {
        return plugin;
    }

    public static boolean hasGSit() {
        return hasGSit;
    }

    public static boolean hasFloodgate() {
        return hasFloodgate;
    }

    public static int getCurrentTick() {
        return tickCount;
    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
