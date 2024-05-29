package org.lushplugins.followers;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.scheduler.BukkitTask;
import org.lushplugins.followers.hooks.EssentialsHook;
import org.lushplugins.followers.hooks.GSitHook;
import org.lushplugins.followers.hooks.PlaceholderAPIHook;
import org.lushplugins.followers.hooks.SimpleSitHook;
import org.lushplugins.followers.listener.FollowerEntityListener;
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
import org.lushplugins.lushlib.LushLib;
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
    public void onLoad() {
        LushLib.getInstance().enable(this);

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
            .reEncodeByDefault(false)
            .checkForUpdates(false)
            .bStats(false);
        PacketEvents.getAPI().load();

        EntityLib.init(
            new SpigotEntityLibPlatform(this),
            new APIConfig(PacketEvents.getAPI()));
    }

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
                new FollowerCreator().registerListeners();

                addHook("Essentials", () -> registerHook(new EssentialsHook()));
                addHook("floodgate", () -> hasFloodgate = true);
                addHook("GSit", () -> registerHook(new GSitHook()));
                addHook("PlaceholderAPI", () -> registerHook(new PlaceholderAPIHook()));
                addHook("SimpleSit", () -> registerHook(new SimpleSitHook()));

                registerCommand(new FollowerCmd());
                registerCommand(new GetHexArmorCmd());
                registerCommand(new DyeCmd());

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
                PacketEvents.getAPI().init();
            } else {
                Followers.getInstance().getLogger().severe("Could not initialise the data. Aborting further plugin setup.");
            }
        });

        heartbeat = Bukkit.getScheduler().runTaskTimer(this, () -> {
            tickCount++;

            if (dataManager != null) {
                dataManager.getOwnedFollowers().forEach(followerEntity -> {
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
        PacketEvents.getAPI().terminate();

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
