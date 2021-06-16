package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerEntity;

import java.util.HashMap;
import java.util.UUID;

public class DataManager {
    private final Storage storage;
    private final HashMap<UUID, FollowerEntity> playerFollowerMap = new HashMap<>();
    private final HashMap<UUID, FollowerUser> uuidToFollowerUser = new HashMap<>();

    public DataManager() {
        String databaseType = ESFollowers.configManager.getDatabaseSection().getString("type");
        if (databaseType.equalsIgnoreCase("mysql")) storage = new MysqlStorage();
        else storage = new YmlStorage();
    }

    public FollowerUser loadFollowerUser(UUID uuid) {
        FollowerUser followerUser = storage.loadFollowerUser(uuid);
        uuidToFollowerUser.put(uuid, followerUser);
        return followerUser;
    }

    public void saveFollowerUser(FollowerUser followerUser) {
        storage.saveFollowerUser(followerUser);
    }

    public FollowerUser getFollowerUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return uuidToFollowerUser.getOrDefault(uuid, new FollowerUser(uuid, player.getName(), "none", "Unnamed", false, false));
    }

    public HashMap<UUID, FollowerEntity> getPlayerFollowerMap() {
        return playerFollowerMap;
    }

    public void putInPlayerFollowerMap(UUID playerUUID, FollowerEntity follower) {
        playerFollowerMap.put(playerUUID, follower);
    }

    public void removeFromPlayerFollowerMap(UUID playerUUID) {
        FollowerUser followerUser = ESFollowers.dataManager.getFollowerUser(playerUUID);
        if (followerUser != null) saveFollowerUser(followerUser);
        playerFollowerMap.remove(playerUUID);
    }

    public void reloadFollowerInventories() {
        for (UUID playerUUID : playerFollowerMap.keySet()) {
            playerFollowerMap.get(playerUUID).reloadInventory();
        }
    }
}
