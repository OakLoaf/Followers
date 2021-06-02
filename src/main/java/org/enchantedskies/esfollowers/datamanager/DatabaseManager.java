package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    public boolean setCoins(Player player, long amount) {
        try (Connection conn = conn(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE player_coins SET coins = ? WHERE uuid = ?"
        )) {
            stmt.setLong(1, amount);
            stmt.setString(2, player.getUniqueId().toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logSQLError("Could not set coins", e);
        }
        return false;
    }
}
