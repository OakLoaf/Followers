package me.dave.followers;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.dave.followers.apis.PlaceholderAPIHook;
import me.dave.followers.apis.SimpleSitHook;
import me.dave.followers.events.WorldEvents;
import me.dave.followers.item.FollowerCreator;
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
import me.dave.followers.storage.Storage;
import me.dave.followers.apis.EssentialsHook;
import me.dave.followers.events.GuiEvents;
import me.dave.followers.events.FollowerUserEvents;

public final class Followers extends JavaPlugin {
    private static Followers plugin;
    public static DataManager dataManager;
    public static ConfigManager configManager;
    public static FollowerManager followerManager;
    private final NamespacedKey followerKey = new NamespacedKey(this, "Follower");
    private static int tickCount;
    private static boolean hasFloodgate = false;

    private void setThreadIOName() {
        Storage.SERVICE.submit(() -> Thread.currentThread().setName("Followers IO Thread"));
    }

    @Override
    public void onEnable() {
        plugin = this;
        ChatColorHandler.enableMiniMessage(true);
        setThreadIOName();
        configManager = new ConfigManager();
        dataManager = new DataManager();
        dataManager.initAsync((successful) -> {
            if (successful) {
                followerManager = new FollowerManager();

                Listener[] listeners = new Listener[]{
                        new FollowerUserEvents(),
                        new GuiEvents(),
                        new WorldEvents(),
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

                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        for (Entity entity : chunk.getEntities()) {
                            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING))
                                entity.remove();
                        }
                    }
                }
            } else {
                Followers.getInstance().getLogger().severe("Could not initialise the data. Aborting further plugin setup.");
            }
        });

        Bukkit.getScheduler().runTaskTimer(plugin, () -> tickCount++, 1, 1);
    }

    @Override
    public void onDisable() {
        Storage.SERVICE.shutdownNow();
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
