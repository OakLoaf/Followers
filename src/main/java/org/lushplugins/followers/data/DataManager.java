package org.lushplugins.followers.data;

import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.entity.OwnedFollower;
import org.lushplugins.followers.storage.MysqlStorage;
import org.lushplugins.followers.storage.Storage;
import org.lushplugins.followers.storage.YmlStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.followers.Followers;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static java.util.Objects.requireNonNull;

public class DataManager {
    private Storage storage;
    private final HashMap<UUID, FollowerUser> followerUsers = new HashMap<>();

    // Safe to use bukkit api in callback.
    public void initAsync(Consumer<Boolean> onComplete) {
        Storage.SERVICE.submit(() -> {
            String databaseType = Followers.getInstance().getConfigManager().getDatabaseType();
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
            followerUsers.put(uuid, user);
            return user;
        }, Storage.SERVICE);
        return future;
    }

    public void unloadFollowerUser(UUID uuid) {
        followerUsers.remove(uuid);
    }

    public void saveFollowerUser(FollowerUser followerUser) {
        Storage.SERVICE.submit(() -> storage.saveFollowerUser(followerUser));
    }

    public @NotNull FollowerUser getFollowerUser(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        FollowerUser followerUser = followerUsers.get(uuid);
        if (followerUser == null) {
            followerUser = new FollowerUser(uuid, player.getName(), "none", Followers.getInstance().getConfigManager().getDefaultNickname(), false, false, false);
        }
        return followerUser;
    }

    public Collection<FollowerUser> getOnlineFollowerUsers() {
        return followerUsers.values();
    }

    public List<OwnedFollower> getOwnedFollowers() {
        List<OwnedFollower> followerEntities = new ArrayList<>();

        // Gets all available FollowerEntities - done via a forEach loop in preference to a stream for minor performance improvements
        getOnlineFollowerUsers().forEach(followerUser -> {
            OwnedFollower follower = followerUser.getFollower();

            if (follower != null) {
                followerEntities.add(follower);
            }
        });

        return followerEntities;
    }

    public void reloadFollowerInventories() {
        for (Map.Entry<UUID, FollowerUser> entry : followerUsers.entrySet()) {
            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                followerUsers.remove(uuid);
                return;
            }

            FollowerUser followerUser = entry.getValue();
            Follower follower = followerUser.getFollower();
            if (follower != null) {
                follower.refreshHandler();
            }
        }
    }
}
