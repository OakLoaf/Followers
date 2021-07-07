package org.enchantedskies.esfollowers.datamanager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.enchantedskies.esfollowers.ESFollowers;
import org.enchantedskies.esfollowers.FollowerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static java.util.Objects.requireNonNull;

public class DataManager {
    private final Storage storage;
    private final HashMap<UUID, FollowerUser> uuidToFollowerUser = new HashMap<>();
    private final HashMap<UUID, FollowerEntity> playerFollowerMap = new HashMap<>();
    private final HashSet<UUID> activeArmorStandsSet = new HashSet<>();


    public DataManager() {
        String databaseType = ESFollowers.configManager.getDatabaseSection().getString("type");
        if (databaseType.equalsIgnoreCase("mysql")) storage = new MysqlStorage();
        else storage = new YmlStorage();
    }

    public CompletableFuture<FollowerUser> loadFollowerUser(UUID uuid) {
        CompletableFuture<FollowerUser> future = new CompletableFuture<>();
        future.completeAsync(() -> {
            final FollowerUser user = storage.loadFollowerUser(uuid);
            uuidToFollowerUser.put(uuid, user);
            return user;
        }, Storage.SERVICE);
        return future;
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

    public HashSet<UUID> getActiveArmorStandsSet() {
        return activeArmorStandsSet;
    }

    public void setActiveArmorStand(UUID uuid) {
        setActiveArmorStand(uuid, true);
    }

    public void setActiveArmorStand(UUID uuid, boolean setActive) {
        if (setActive) activeArmorStandsSet.add(uuid);
        else activeArmorStandsSet.remove(uuid);
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
