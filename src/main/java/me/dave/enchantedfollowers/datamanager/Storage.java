package me.dave.enchantedfollowers.datamanager;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Storage {
    ExecutorService SERVICE = Executors.newSingleThreadExecutor();
    FollowerUser loadFollowerUser(UUID uuid);
    void saveFollowerUser(FollowerUser followerUser);
    boolean init();
}
