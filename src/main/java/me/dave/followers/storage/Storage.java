package me.dave.followers.storage;

import me.dave.followers.data.FollowerUser;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Storage {
    ExecutorService SERVICE = Executors.newSingleThreadExecutor();
    FollowerUser loadFollowerUser(UUID uuid);
    void saveFollowerUser(FollowerUser followerUser);
    boolean init();
}
