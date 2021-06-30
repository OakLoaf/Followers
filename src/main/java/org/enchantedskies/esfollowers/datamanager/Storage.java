package org.enchantedskies.esfollowers.datamanager;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {
    FollowerUser loadFollowerUser(UUID uuid);
    CompletableFuture<FollowerUser> loadFollowerUserAsync(UUID uuid);
    void saveFollowerUser(FollowerUser followerUser);
}
