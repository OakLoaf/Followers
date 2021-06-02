package org.enchantedskies.esfollowers.datamanager;

import java.util.UUID;

public interface Storage {
    void saveFollowerUser(FollowerUser followerUser);
    void loadFollowerUser(UUID uuid);
}
