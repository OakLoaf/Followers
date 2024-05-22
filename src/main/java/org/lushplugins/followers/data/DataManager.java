package org.lushplugins.followers.data;

import org.lushplugins.followers.storage.MysqlStorage;
import org.lushplugins.followers.storage.Storage;
import org.lushplugins.followers.storage.YmlStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.entity.FollowerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
            Bukkit.getScheduler().runTask(Followers.getInstance(), () -> onComplete.accept(init));
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

    public @NotNull FollowerUser getFollowerUser(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        FollowerUser followerUser = uuidToFollowerUser.get(uuid);
        if (followerUser == null) {
            followerUser = new FollowerUser(uuid, player.getName(), "none", "Unnamed", false, false, false);
        }
        return followerUser;
    }

    public Collection<FollowerUser> getOnlineFollowerUsers() {
        return uuidToFollowerUser.values();
    }

    public List<FollowerEntity> getAllFollowerEntities() {
        List<FollowerEntity> followerEntities = new ArrayList<>();

        // Gets all available FollowerEntities - done via a forEach loop in preference to a stream for minor performance improvements
        Followers.dataManager.getOnlineFollowerUsers().forEach(followerUser -> {
            FollowerEntity followerEntity = followerUser.getFollowerEntity();

            if (followerEntity != null) {
                followerEntities.add(followerEntity);
            }
        });

        return followerEntities;
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
        uuidToFollowerUser.keySet().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                uuidToFollowerUser.remove(uuid);
                return;
            }

            FollowerEntity followerEntity = getFollowerUser(player).getFollowerEntity();
            if (followerEntity != null) {
                followerEntity.reloadInventory();
            }
        });
    }
}