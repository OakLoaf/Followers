package org.enchantedskies.esfollowers.datamanager;

import java.util.UUID;

public interface Storage {
    FollowerUser loadFollowerUser(UUID uuid);
    void saveFollowerUser(FollowerUser followerUser);
}
