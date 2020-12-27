package org.enchantedskies.esfollowers.datamanager;

import org.enchantedskies.esfollowers.ESFollowers;

import java.util.UUID;

public class FollowerUser {
    private final UUID uuid;
    private String username;
    private boolean isOn;
    private String follower;

    public FollowerUser(UUID uuid, String username, String follower, boolean followerIsEnabled) {
        this.uuid = uuid;
        this.username = username;
        this.isOn = followerIsEnabled;
        this.follower = follower;
    }

    public void setUsername(String username) {
        this.username = username;
        ESFollowers.dataManager.saveFollowerUser(this);
    }

    public void setFollower(String follower) {
        this.follower = follower;
        ESFollowers.dataManager.saveFollowerUser(this);
    }

    public void setFollowerEnabled(boolean followerIsEnabled) {
        this.isOn = followerIsEnabled;
        ESFollowers.dataManager.saveFollowerUser(this);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFollower() {
        return this.follower;
    }

    public boolean isFollowerEnabled() {
        return this.isOn;
    }
}
