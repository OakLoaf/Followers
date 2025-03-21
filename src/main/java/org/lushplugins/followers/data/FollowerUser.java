package org.lushplugins.followers.data;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.config.FollowerHandler;
import org.lushplugins.followers.entity.OwnedFollower;
import org.lushplugins.followers.entity.poses.FollowerPose;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.followers.entity.tasks.TaskId;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class FollowerUser {
    private static final Random random = new Random();
    private final UUID uuid;
    private String username;
    private boolean enabled;
    private String followerType;
    private boolean randomType;
    private String displayName;
    private boolean nameIsOn;

    private OwnedFollower follower;
    private boolean afk = false;
    private boolean posing = false;
    private boolean hidden = false;

    public FollowerUser(UUID uuid, String username, String followerType, String followerDisplayName, boolean followerNameEnabled, boolean followerEnabled, boolean randomFollower) {
        this.uuid = uuid;
        this.username = username;
        this.enabled = followerEnabled;
        this.followerType = followerType;
        this.displayName = followerDisplayName;
        this.nameIsOn = followerNameEnabled;
        this.randomType = randomFollower;
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
        Followers.getInstance().getDataManager().saveFollowerUser(this);
    }

    public boolean isFollowerEnabled() {
        return this.enabled;
    }

    public void setFollowerEnabled(boolean followerIsEnabled) {
        this.enabled = followerIsEnabled;
        Followers.getInstance().getDataManager().saveFollowerUser(this);

        if (followerIsEnabled) {
            if (follower == null || !follower.isSpawned()) {
                spawnFollower();
            }
        } else {
            if (follower != null) {
                follower.despawn();
            }
        }
    }

    public String getFollowerTypeName() {
        return this.followerType;
    }

    public FollowerHandler getFollowerType() {
        return followerType != null ? Followers.getInstance().getFollowerManager().getFollower(followerType) : null;
    }

    public void setFollowerType(String followerType) {
        this.followerType = followerType;
        Followers.getInstance().getDataManager().saveFollowerUser(this);

        if (follower != null) {
            follower.setType(followerType);
        }
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        Followers.getInstance().getDataManager().saveFollowerUser(this);

        if (follower != null) {
            follower.setDisplayName(displayName);
        }
    }

    public boolean isDisplayNameEnabled() {
        return this.nameIsOn;
    }

    public void setDisplayNameEnabled(boolean nameIsEnabled) {
        this.nameIsOn = nameIsEnabled;
        Followers.getInstance().getDataManager().saveFollowerUser(this);

        if (follower != null) {
            follower.setDisplayName(nameIsEnabled ? displayName : null);
        }
    }

    public boolean isRandomType() {
        return this.randomType;
    }

    public String getRandomType() {
        List<String> followerTypes = getOwnedFollowerNames();
        return followerTypes.get(random.nextInt(followerTypes.size()));
    }

    public void setRandom(boolean randomize) {
        this.randomType = randomize;
        Followers.getInstance().getDataManager().saveFollowerUser(this);
    }

    public void randomiseFollowerType() {
        if (follower != null) {
            follower.setType(getRandomType());
        }
    }

    public List<String> getOwnedFollowerNames() {
        List<String> followers = new ArrayList<>();
        Player player = getPlayer();
        if (player == null) {
            return followers;
        }

        for (String followerName : Followers.getInstance().getFollowerManager().getFollowerNames()) {
            if (player.hasPermission("followers." + followerName.toLowerCase().replaceAll(" ", "_"))) {
                followers.add(followerName);
            }
        }
        return followers;
    }

    public boolean isAfk() {
        return afk;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;

        if (follower == null || posing) return;
        if (afk) follower.setPose(FollowerPose.SITTING);
        else follower.setPose(FollowerPose.DEFAULT);
    }

    public boolean isPosing() {
        return posing;
    }

    public void setPose(FollowerPose pose) {
        this.posing = (pose != null && !pose.equals(FollowerPose.DEFAULT));

        if (follower == null) {
            return;
        }

        if (posing) {
            follower.setPose(pose);
        } else if (!afk) {
            follower.setPose(FollowerPose.DEFAULT);
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hide) {
        if (this.hidden == hide) {
            return;
        }

        if (hide) {
            if (follower != null && follower.isSpawned()) {
                follower.despawn();
            }
        } else if (enabled) {
            if (follower == null || !follower.isSpawned()) {
                spawnFollower();
            }
        }

        this.hidden = hide;
    }

    public OwnedFollower getFollower() {
        return follower;
    }

    public void refreshFollower() {
        if (follower != null) {
            follower.refreshHandler();
        }
    }

    public void respawnFollower() {
        if (follower != null) {
            follower.despawn();
        }

        Bukkit.getScheduler().runTaskLater(Followers.getInstance(), this::spawnFollower, 5);
    }


    public void spawnFollower() {
        Player player = getPlayer();
        if (player == null || player.isDead()) {
            return;
        }

        if (follower == null) {
            follower = new OwnedFollower(player, randomType ? getRandomType() : followerType);
            follower.setWorld(player.getWorld());
            follower.setDisplayName(isDisplayNameEnabled() ? displayName : null);
        }

        if (follower.spawn(player.getWorld(), SpigotConversionUtil.fromBukkitLocation(player.getLocation().add(1.5, 0, 1.5)))) {
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> follower.addTask(TaskId.VISIBILITY), 5);
        }
    }
}
