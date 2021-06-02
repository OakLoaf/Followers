package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerArmorStand;

import java.util.HashMap;
import java.util.UUID;

public class DataManager {
    private final Storage storage;
    private final HashMap<UUID, UUID> playerFollowerMap = new HashMap<>();
    private final HashMap<UUID, FollowerUser> uuidToFollowerUser = new HashMap<>();

    public DataManager() {
        String databaseType = ESFollowers.configManager.getDatabaseType();
        if (databaseType.equalsIgnoreCase("mysql")) storage = new MysqlStorage();
        else storage = new YmlStorage();
    }

    public void loadFollowerUser(UUID uuid) {
        uuidToFollowerUser.put(uuid, storage.loadFollowerUser(uuid));
    }

    public void saveFollowerUser(FollowerUser followerUser) {
        storage.saveFollowerUser(followerUser);
    }

    public FollowerUser getFollowerUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return uuidToFollowerUser.getOrDefault(uuid, new FollowerUser(uuid, player.getName(), "none", "Unnamed", false, false));
    }

    public HashMap<UUID, UUID> getPlayerFollowerMap() {
        return playerFollowerMap;
    }

    public void putInPlayerFollowerMap(UUID playerUUID, UUID followerUUID) {
        playerFollowerMap.put(playerUUID, followerUUID);
    }

    public void removeFromPlayerFollowerMap(UUID playerUUID) {
        playerFollowerMap.remove(playerUUID);
    }

    public void reloadFollowerInventories() {
        for (UUID playerUUID : playerFollowerMap.keySet()) {
            UUID followerUUID = playerFollowerMap.get(playerUUID);
            new FollowerArmorStand(getFollowerUser(playerUUID).getFollower(), (ArmorStand) Bukkit.getEntity(followerUUID));
        }
    }
}
