package org.lushplugins.followers;

import org.bukkit.scheduler.BukkitTask;
import org.lushplugins.followers.hooks.EssentialsHook;
import org.lushplugins.followers.hooks.GSitHook;
import org.lushplugins.followers.hooks.PlaceholderAPIHook;
import org.lushplugins.followers.hooks.SimpleSitHook;
import org.lushplugins.followers.listener.FollowerEntityListener;
import org.lushplugins.followers.listener.WorldListener;
import org.lushplugins.followers.item.FollowerCreator;
import org.lushplugins.followers.data.ConfigManager;
import org.lushplugins.followers.data.DataManager;
import org.lushplugins.followers.data.FollowerManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;

import org.bukkit.persistence.PersistentDataType;
import org.lushplugins.followers.commands.DyeCmd;
import org.lushplugins.followers.commands.FollowerCmd;
import org.lushplugins.followers.commands.GetHexArmorCmd;
import org.lushplugins.followers.storage.Storage;
import org.lushplugins.followers.listener.InventoryListener;
import org.lushplugins.followers.listener.FollowerUserListener;
import org.lushplugins.lushlib.hook.Hook;
import org.lushplugins.lushlib.plugin.SpigotPlugin;

public final class Followers extends SpigotPlugin {
    private static Followers plugin;

    private NamespacedKey followerKey;
    private ConfigManager configManager;
    private DataManager dataManager;
    private FollowerManager followerManager;
    private BukkitTask heartbeat;
    private int tickCount;
    private boolean hasFloodgate = false;

    @Override
    public void onEnable() {
        plugin = this;
        followerKey = new NamespacedKey(this, "Follower");
        Storage.SERVICE.submit(() -> Thread.currentThread().setName("Followers IO Thread"));

        configManager = new ConfigManager();
        dataManager = new DataManager();
        dataManager.initAsync((successful) -> {
            if (successful) {
                followerManager = new FollowerManager();

                new FollowerEntityListener().registerListeners();
                new FollowerUserListener().registerListeners();
                new InventoryListener().registerListeners();
                new WorldListener().registerListeners();
                new FollowerCreator().registerListeners();

                addHook("Essentials", () -> registerHook(new EssentialsHook()));
                addHook("floodgate", () -> hasFloodgate = true);
                addHook("GSit", () -> registerHook(new GSitHook()));
                addHook("PlaceholderAPI", () -> registerHook(new PlaceholderAPIHook()));
                addHook("SimpleSit", () -> registerHook(new SimpleSitHook()));

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

                getHooks().forEach(Hook::enable);
            } else {
                Followers.getInstance().getLogger().severe("Could not initialise the data. Aborting further plugin setup.");
            }
        });

        heartbeat = Bukkit.getScheduler().runTaskTimer(this, () -> {
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
        }, 1, 1);
    }

    @Override
    public void onDisable() {
        if (heartbeat != null) {
            heartbeat.cancel();
            heartbeat = null;
        }


        Storage.SERVICE.shutdownNow();
    }

    public NamespacedKey getFollowerKey() {
        return followerKey;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public FollowerManager getFollowerManager() {
        return followerManager;
    }

    public int getCurrentTick() {
        return tickCount;
    }

    public boolean hasFloodgate() {
        return hasFloodgate;
    }

    public static Followers getInstance() {
        return plugin;
    }
}
