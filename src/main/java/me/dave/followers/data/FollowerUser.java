package me.dave.followers.data;

import me.dave.followers.Followers;
import me.dave.followers.entity.FollowerEntity;
import me.dave.followers.entity.poses.FollowerPose;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class FollowerUser {
    private static final Random random = new Random();
    private final UUID uuid;
    private String username;
    private boolean isEnabled;
    private String followerType;
    private boolean randomType;
    private String displayName;
    private boolean nameIsOn;
    private FollowerEntity followerEntity;
    private boolean afk = false;
    private boolean posing = false;

    public FollowerUser(UUID uuid, String username, String followerType, String followerDisplayName, boolean followerNameEnabled, boolean followerIsEnabled, boolean randomFollower) {
        this.uuid = uuid;
        this.username = username;
        this.isEnabled = followerIsEnabled;
        this.followerType = followerType;
        this.displayName = followerDisplayName;
        this.nameIsOn = followerNameEnabled;
        this.randomType = randomFollower;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFollowerType() {
        return this.followerType;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isDisplayNameEnabled() {
        return this.nameIsOn;
    }

    public boolean isFollowerEnabled() {
        return this.isEnabled;
    }

    public boolean isRandomType() {
        return this.randomType;
    }

    public void setUsername(String username) {
        this.username = username;
        Followers.dataManager.saveFollowerUser(this);
    }

    public void setFollowerType(String followerType) {
        this.followerType = followerType;
        Followers.dataManager.saveFollowerUser(this);
    }

    public void setRandom(boolean randomize) {
        this.randomType = randomize;
        Followers.dataManager.saveFollowerUser(this);
    }

    public List<String> getOwnedFollowerNames() {
        List<String> followers = new ArrayList<>();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return followers;

        for (String followerName : Followers.followerManager.getFollowerNames()) {
            if (player.hasPermission("followers." + followerName.toLowerCase().replaceAll(" ", "_"))) followers.add(followerName);
        }
        return followers;
    }

    public void randomizeFollowerType() {
        List<String> followerTypes = getOwnedFollowerNames();
        if (followerEntity == null) return;
        followerEntity.setFollowerType(followerTypes.get(random.nextInt(followerTypes.size())));
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        Followers.dataManager.saveFollowerUser(this);
    }

    public void setDisplayNameEnabled(boolean nameIsEnabled) {
        this.nameIsOn = nameIsEnabled;
        Followers.dataManager.saveFollowerUser(this);
    }

    public void setFollowerEnabled(boolean followerIsEnabled) {
        this.isEnabled = followerIsEnabled;
        Followers.dataManager.saveFollowerUser(this);
    }

    public boolean isAfk() {
        return afk;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;

        if (followerEntity == null || posing) return;
        if (afk) followerEntity.setPose(FollowerPose.SITTING);
        else followerEntity.setPose(FollowerPose.DEFAULT);
    }

    public boolean isPosing() {
        return posing;
    }

    public void setPose(FollowerPose pose) {
        this.posing = (pose != null && !pose.equals(FollowerPose.DEFAULT));

        if (followerEntity == null) return;
        if (posing) followerEntity.setPose(pose);
        else if (!afk) followerEntity.setPose(FollowerPose.DEFAULT);
    }

    public FollowerEntity getFollowerEntity() {
        return followerEntity;
    }

    public void refreshFollowerEntity() {
        if (followerEntity != null) followerEntity.reloadInventory();
    }

    public void spawnFollowerEntity() {
        removeFollowerEntity();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || player.isDead()) return;

        Chunk chunk = player.getLocation().getChunk();
        if (!chunk.isLoaded()) {
            if (!chunk.load()) return;
        }

        followerEntity = new FollowerEntity(player, followerType);
        if (randomType) randomizeFollowerType();
    }

    public void respawnFollowerEntity() {
        spawnFollowerEntity();
    }

    public void removeFollowerEntity() {
        if (followerEntity == null || !followerEntity.isAlive()) return;
        followerEntity.kill();
        followerEntity = null;
    }

    public void disableFollowerEntity() {
        setFollowerEnabled(false);
        removeFollowerEntity();
    }
}
