package me.dave.followers.data;

import me.dave.followers.storage.MysqlStorage;
import me.dave.followers.storage.Storage;
import me.dave.followers.storage.YmlStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static java.util.Objects.requireNonNull;

public class DataManager {
    private Storage storage;
    private final HashMap<UUID, FollowerUser> uuidToFollowerUser = new HashMap<>();
    private final HashSet<UUID> activeArmorStandsSet = new HashSet<>();

    // Safe to use bukkit api in callback.
    public void initAsync(Consumer<Boolean> onComplete) {
        Storage.SERVICE.submit(() -> {
            String databaseType = Followers.configManager.getDatabaseType();
            final String errStr = "Could not read database type! Check config";
            if (requireNonNull(databaseType, errStr).equalsIgnoreCase("mysql")) {
                storage = new MysqlStorage();
            } else {
                storage = new YmlStorage();
            }
            final boolean init = storage.init();
            new BukkitRunnable() {
                @Override
                public void run() {
                    onComplete.accept(init);
                }
            }.runTask(Followers.getInstance());
        });
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

    public void unloadFollowerUser(UUID uuid) {
        uuidToFollowerUser.remove(uuid);
    }

    public void saveFollowerUser(FollowerUser followerUser) {
        Storage.SERVICE.submit(() -> storage.saveFollowerUser(followerUser));
    }

    public FollowerUser getFollowerUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        FollowerUser followerUser = uuidToFollowerUser.get(uuid);
        if (followerUser == null) followerUser = new FollowerUser(uuid, player.getName(), "none", "Unnamed", false, false);
        return followerUser;
    }

    public HashSet<UUID> getActiveArmorStandsSet() {
        return activeArmorStandsSet;
    }

    public void addActiveArmorStand(UUID uuid) {
        activeArmorStandsSet.add(uuid);
    }

    public void removeActiveArmorStand(UUID uuid) {
        activeArmorStandsSet.remove(uuid);
    }


    public void reloadFollowerInventories() {
        for (UUID playerUUID : uuidToFollowerUser.keySet()) {
            FollowerEntity followerEntity = getFollowerUser(playerUUID).getFollowerEntity();
            if (followerEntity != null) followerEntity.reloadInventory();
        }
    }
}
