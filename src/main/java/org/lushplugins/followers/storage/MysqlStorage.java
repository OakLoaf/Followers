package org.lushplugins.followers.storage;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.lushplugins.followers.data.DataManager;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.Followers;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MysqlStorage implements Storage {
    private MysqlDataSource dataSource;


    @Override
    public boolean init() {
        ConfigurationSection databaseSection = Followers.getInstance().getConfigManager().getDatabaseSection();
        String dbName = databaseSection.getString("name");
        String dbHost = databaseSection.getString("host");
        int dbPort = databaseSection.getInt("port");
        String dbUser = databaseSection.getString("user");
        String dbPass = databaseSection.getString("password", "");
        dataSource = initMySQLDataSource(dbName, dbHost, dbPort, dbUser, dbPass);
        String setup;
        try (InputStream in = DataManager.class.getClassLoader().getResourceAsStream("dbsetup.sql")) {
            setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            Followers.getInstance().getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            e.printStackTrace();
            return false;
        }
        String[] queries = setup.split(";");
        for (String query : queries) {
            if (query.isEmpty()) {
                continue;
            }

            try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        Followers.getInstance().getLogger().info(ChatColorHandler.translate("&2Database setup complete."));
        return true;
    }

    public Connection conn() {
        try {
            return dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    public FollowerUser loadFollowerUser(UUID uuid) {
        try (Connection conn = conn(); PreparedStatement stmt = conn.prepareStatement(
            "SELECT * FROM follower_users WHERE uuid = ?;"
        )) {
            stmt.setString(1, uuid.toString());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return new FollowerUser(
                    uuid,
                    resultSet.getString("name"),
                    resultSet.getString("follower"),
                    resultSet.getString("followerDisplayName"),
                    resultSet.getBoolean("followerNameEnabled"),
                    resultSet.getBoolean("followerEnabled"),
                    resultSet.getBoolean("randomFollower")
                );
            } else {
                FollowerUser newFollowerUser = new FollowerUser(uuid, Bukkit.getPlayer(uuid).getName(), "none", Followers.getInstance().getConfigManager().getDefaultNickname(), false, false, false);
                saveFollowerUser(newFollowerUser);
                return newFollowerUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveFollowerUser(FollowerUser followerUser) {
        Bukkit.getScheduler().runTaskAsynchronously(Followers.getInstance(), () -> {
            try (Connection conn = conn(); PreparedStatement stmt = conn.prepareStatement(
                "REPLACE INTO follower_users(uuid, name, follower, followerDisplayName, followerNameEnabled, followerEnabled, randomFollower) VALUES(?, ?, ?, ?, ?, ?, ?);"
            )) {
                stmt.setString(1, followerUser.getUniqueId().toString());
                stmt.setString(2, followerUser.getUsername());
                stmt.setString(3, followerUser.getFollowerTypeName());
                stmt.setString(4, followerUser.getDisplayName());
                stmt.setBoolean(5, followerUser.isDisplayNameEnabled());
                stmt.setBoolean(6, followerUser.isFollowerEnabled());
                stmt.setBoolean(7, followerUser.isRandomType());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    private MysqlDataSource initMySQLDataSource(String dbName, String host, int port, String user, String password) {
        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setDatabaseName(dbName);
        dataSource.setServerName(host);
        dataSource.setPortNumber(port);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        testDataSource(dataSource);
        return dataSource;
    }

    private void testDataSource(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1000)) {
                throw new SQLException("Could not establish database connection.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
