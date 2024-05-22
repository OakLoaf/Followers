package org.lushplugins.followers;

import org.lushplugins.followers.hooks.EssentialsHook;
import org.lushplugins.followers.hooks.GSitHook;
import org.lushplugins.followers.hooks.PlaceholderAPIHook;
import org.lushplugins.followers.hooks.SimpleSitHook;
import org.lushplugins.followers.listener.FollowerEntityListener;
import org.lushplugins.followers.listener.WorldListener;
import org.lushplugins.followers.item.FollowerCreator;
import org.lushplugins.followers.utils.skullcreator.LegacySkullCreator;
import org.lushplugins.followers.utils.skullcreator.NewSkullCreator;
import org.lushplugins.followers.utils.skullcreator.SkullCreator;
import org.lushplugins.followers.data.ConfigManager;
import org.lushplugins.followers.data.DataManager;
import org.lushplugins.followers.data.FollowerManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.followers.commands.DyeCmd;
import org.lushplugins.followers.commands.FollowerCmd;
import org.lushplugins.followers.commands.GetHexArmorCmd;
import org.lushplugins.followers.storage.Storage;
import org.lushplugins.followers.listener.InventoryListener;
import org.lushplugins.followers.listener.FollowerUserListener;
import org.bukkit.scheduler.BukkitRunnable;

public final class Followers extends JavaPlugin {
    private static SkullCreator skullCreator;

    private static Followers plugin;
    private static NamespacedKey followerKey;
    public static DataManager dataManager;
    public static ConfigManager configManager;
    public static FollowerManager followerManager;
    private static int tickCount;
    private static final BukkitRunnable ticker;
    private static boolean hasFloodgate = false;

    static {
        try {
            String version = Bukkit.getBukkitVersion();
            if (version.contains("1.16") || version.contains("1.17") || version.contains("1.18")) {
                skullCreator = new LegacySkullCreator();
            } else {
                skullCreator = new NewSkullCreator();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ticker = new BukkitRunnable() {
            @Override
            public void run() {
                tickCount++;

                if (dataManager != null) {
                    dataManager.getAllFollowerEntities().forEach(followerEntity -> {
                        try {
                            followerEntity.tick();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        };
    }

    private void setThreadIOName() {
        Storage.SERVICE.submit(() -> Thread.currentThread().setName("Followers IO Thread"));
    }

    @Override
    public void onEnable() {
        plugin = this;
        followerKey = new NamespacedKey(this, "Follower");

        setThreadIOName();
        configManager = new ConfigManager();
        dataManager = new DataManager();
        dataManager.initAsync((successful) -> {
            if (successful) {
                followerManager = new FollowerManager();

                Listener[] listeners = new Listener[]{
                        new FollowerEntityListener(),
                        new FollowerUserListener(),
                        new InventoryListener(),
                        new WorldListener(),
                        new FollowerCreator()
                };
                registerEvents(listeners);

                PluginManager pluginManager = getServer().getPluginManager();
                if (pluginManager.getPlugin("Essentials") != null) {
                    pluginManager.registerEvents(new EssentialsHook(), this);
                    getLogger().info("Found plugin \"Essentials\". Essentials support enabled.");
                }

                if (this.getServer().getPluginManager().getPlugin("Floodgate") != null) {
                    hasFloodgate = true;
                    getLogger().info("Found plugin \"Floodgate\". Floodgate support enabled.");
                }

                if (pluginManager.getPlugin("GSit") != null) {
                    pluginManager.registerEvents(new GSitHook(), this);
                    getLogger().info("Found plugin \"GSit\". GSit support enabled.");
                }

                if (pluginManager.getPlugin("PlaceholderAPI") != null) {
                    new PlaceholderAPIHook().register();
                    getLogger().info("Found plugin \"PlaceholderAPI\". PlaceholderAPI support enabled.");
                }

                if (pluginManager.getPlugin("SimpleSit") != null) {
                    pluginManager.registerEvents(new SimpleSitHook(), this);
                    getLogger().info("Found plugin \"SimpleSit\". SimpleSit support enabled.");
                }

                getCommand("followers").setExecutor(new FollowerCmd());
                getCommand("gethexarmor").setExecutor(new GetHexArmorCmd());
                getCommand("dye").setExecutor(new DyeCmd());

                Bukkit.getWorlds().forEach(world -> {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        for (Entity entity : chunk.getEntities()) {
                            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) {
                                entity.remove();
                            }
                        }
                    }
                });
            } else {
                Followers.getInstance().getLogger().severe("Could not initialise the data. Aborting further plugin setup.");
            }
        });

        ticker.runTaskTimer(plugin, 1, 1);
    }

    @Override
    public void onDisable() {
        ticker.cancel();
        Storage.SERVICE.shutdownNow();
    }

    public NamespacedKey getFollowerKey() {
        return followerKey;
    }

    public boolean callEvent(Event event) {
        getServer().getPluginManager().callEvent(event);
        if (event instanceof Cancellable cancellable) {
            return !cancellable.isCancelled();
        } else {
            return true;
        }
    }

    public static SkullCreator getSkullCreator() {
        return skullCreator;
    }

    public static Followers getInstance() {
        return plugin;
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
