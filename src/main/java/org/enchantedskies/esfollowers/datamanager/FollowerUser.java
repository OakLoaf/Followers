package org.enchantedskies.esfollowers.datamanager;

import org.enchantedskies.esfollowers.ESFollowers;

import java.util.UUID;

public class FollowerUser {
    private final UUID uuid;
    private String username;
    private boolean isOn;
    private String follower;
    private String displayName;
    private boolean nameIsOn;

    public FollowerUser(UUID uuid, String username, String follower, String followerDisplayName, boolean followerNameEnabled, boolean followerIsEnabled) {
        this.uuid = uuid;
        this.username = username;
        this.isOn = followerIsEnabled;
        this.follower = follower;
        this.displayName = followerDisplayName;
        this.nameIsOn = followerNameEnabled;
    }

    public void setUsername(String username) {
        this.username = username;
        ESFollowers.dataManager.saveFollowerUser(this);
    }

    public void setFollower(String follower) {
        this.follower = follower;
        ESFollowers.dataManager.saveFollowerUser(this);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        ESFollowers.dataManager.saveFollowerUser(this);
    }

    public void setDisplayNameEnabled(boolean nameIsEnabled) {
        this.nameIsOn = nameIsEnabled;
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

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isDisplayNameEnabled() {
        return this.nameIsOn;
    }

    public boolean isFollowerEnabled() {
        return this.isOn;
    }
}
