package org.enchantedskies.esfollowers;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.enchantedskies.esfollowers.commands.FollowerCmd;
import org.enchantedskies.esfollowers.commands.GetHexArmorCmd;
import org.enchantedskies.esfollowers.datamanager.ConfigManager;
import org.enchantedskies.esfollowers.datamanager.DataManager;
import org.enchantedskies.esfollowers.datamanager.Storage;
import org.enchantedskies.esfollowers.datamanager.YmlDataManager;
import org.enchantedskies.esfollowers.events.EssentialsEvents;
import org.enchantedskies.esfollowers.events.FollowerGUIEvents;
import org.enchantedskies.esfollowers.events.FollowerUserEvents;
import org.enchantedskies.esfollowers.utils.SkullCreator;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class ESFollowers extends JavaPlugin implements Listener {
    private static ESFollowers plugin;
    public static String prefix = "§8§l[§d§lES§8§l] §r";
    public static Storage dataManager;
    public static ConfigManager configManager;
    public static SkullCreator skullCreator = new SkullCreator();
    private MysqlDataSource dataSource;
    private final HashSet<UUID> guiPlayerSet = new HashSet<>();
    private final NamespacedKey followerKey = new NamespacedKey(this, "ESFollower");

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        configManager = new ConfigManager();

        String databaseType = configManager.getDatabaseType();
        if (databaseType.equalsIgnoreCase("mysql")) {
            try {
                dataSource = initMySQLDataSource();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            dataManager = new DataManager(dataSource);
        } else  {
            dataManager = new YmlDataManager();
        }

        Listener[] listeners = new Listener[] {
            this,
            new FollowerUserEvents(followerKey),
            new FollowerGUIEvents(guiPlayerSet, followerKey),
            new FollowerCreator(),
        };
        registerEvents(listeners);

        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("Essentials") != null) {
            pluginManager.registerEvents(new EssentialsEvents(), this);
        } else {
            getLogger().info("Essentials plugin not found. Continuing without Essentials.");
        }
        getCommand("followers").setExecutor(new FollowerCmd(guiPlayerSet));
        getCommand("gethexarmor").setExecutor(new GetHexArmorCmd());

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (dataManager.getPlayerFollowerMap().containsValue(entity.getUniqueId())) continue;
            if (entity.getPersistentDataContainer().has(followerKey, PersistentDataType.STRING)) entity.remove();
        }
    }

    public static ESFollowers getInstance() {
        return plugin;
    }

    public void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    private void initDb() throws SQLException, IOException {
        String setup;
        try (InputStream in = getClassLoader().getResourceAsStream("dbsetup.sql")) {
            setup = new String(in.readAllBytes());
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            throw e;
        }
        String[] queries = setup.split(";");
        for (String query : queries) {
            if (query.isBlank()) continue;
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            }
        }
        getLogger().info("§2Database setup complete.");
    }

    private MysqlDataSource initMySQLDataSource() throws SQLException {
        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
        testDataSource(dataSource);
        return dataSource;
    }

    private void testDataSource(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1000)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }
}
