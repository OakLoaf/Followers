package org.lushplugins.followers;

import com.github.retrooper.packetevents.PacketEvents;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.scheduler.BukkitTask;
import org.lushplugins.followers.api.events.FollowerTickEvent;
import org.lushplugins.followers.entity.EyeHeightRegistry;
import org.lushplugins.followers.hook.EssentialsHook;
import org.lushplugins.followers.hook.GSitHook;
import org.lushplugins.followers.hook.PlaceholderAPIHook;
import org.lushplugins.followers.hook.SimpleSitHook;
import org.lushplugins.followers.listener.FollowerEntityListener;
import org.lushplugins.followers.config.ConfigManager;
import org.lushplugins.followers.data.DataManager;
import org.lushplugins.followers.config.FollowerManager;
import org.bukkit.*;

import org.lushplugins.followers.command.DyeCmd;
import org.lushplugins.followers.command.FollowerCmd;
import org.lushplugins.followers.command.GetHexArmorCmd;
import org.lushplugins.followers.listener.AnvilMenuListener;
import org.lushplugins.followers.listener.PacketListener;
import org.lushplugins.followers.storage.Storage;
import org.lushplugins.followers.listener.PlayerListener;
import org.lushplugins.followers.utils.WebUtils;
import org.lushplugins.lushlib.LushLib;
import org.lushplugins.lushlib.hook.Hook;
import org.lushplugins.lushlib.plugin.SpigotPlugin;
import org.lushplugins.lushlib.utils.SimpleItemStack;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class Followers extends SpigotPlugin {
    private static Followers plugin;

    private final EyeHeightRegistry eyeHeightRegistry = new EyeHeightRegistry();
    private ConfigManager configManager;
    private DataManager dataManager;
    private FollowerManager followerManager;
    private BukkitTask heartbeat;
    private int tickCount;
    private boolean hasFloodgate = false;

    static {
        if (Bukkit.getPluginManager().getPlugin("packetevents") == null) {
            try {
                File output = WebUtils.downloadFile(
                    new URL("https://ci.codemc.io/job/retrooper/job/packetevents/600/artifact/spigot/build/libs/packetevents-spigot-2.6.0.jar"),
                    Bukkit.getUpdateFolderFile().getParentFile(),
                    "packetevents-spigot.jar");

                Bukkit.getPluginManager().loadPlugin(output);
            } catch (IOException | InvalidPluginException | InvalidDescriptionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoad() {
        LushLib.getInstance().enable(this);
        SimpleItemStack.setDefaultMaterial(Material.EGG);

        EntityLib.init(
            new SpigotEntityLibPlatform(this),
            new APIConfig(PacketEvents.getAPI()));
    }

    @Override
    public void onEnable() {
        plugin = this;
        Storage.SERVICE.submit(() -> Thread.currentThread().setName("Followers IO Thread"));

        configManager = new ConfigManager();
        dataManager = new DataManager();
        dataManager.initAsync((successful) -> {
            if (successful) {
                followerManager = new FollowerManager();

                registerListeners(
                    new FollowerEntityListener(),
                    new AnvilMenuListener(),
                    new PlayerListener()
                );

                addHook("Essentials", () -> registerHook(new EssentialsHook()));
                addHook("floodgate", () -> hasFloodgate = true);
                addHook("GSit", () -> registerHook(new GSitHook()));
                addHook("PlaceholderAPI", () -> registerHook(new PlaceholderAPIHook()));
                addHook("SimpleSit", () -> registerHook(new SimpleSitHook()));

                registerCommand(new FollowerCmd());
                registerCommand(new GetHexArmorCmd());
                registerCommand(new DyeCmd());

                getHooks().forEach(Hook::enable);

                PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());
            } else {
                Followers.getInstance().getLogger().severe("Could not initialise the data. Aborting further plugin setup.");
            }
        });

        heartbeat = Bukkit.getScheduler().runTaskTimer(this, () -> {
            tickCount++;

            if (dataManager != null) {
                dataManager.getOwnedFollowers().forEach(follower -> Followers.getInstance().callEvent(new FollowerTickEvent(follower)));
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

    public EyeHeightRegistry getEyeHeightRegistry() {
        return eyeHeightRegistry;
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
