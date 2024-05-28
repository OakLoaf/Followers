package org.lushplugins.followers.entity.tasks;

import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.FollowerEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ValidateTask extends FollowerTask {
    private static final HashMap<UUID, Integer> attemptsMap = new HashMap<>();
    private final Player player;

    public ValidateTask(String id, Player player) {
        super(id);
        this.player = player;
    }

    @Override
    public void tick(FollowerEntity follower) {
        if (!follower.isAlive()) {
            cancel(follower);
            return;
        }

        if (!follower.isEntityValid()) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            UUID uuid = player.getUniqueId();

            int attempts = attemptsMap.getOrDefault(uuid, 0);
            if (attempts >= Followers.getInstance().getConfigManager().getMaxRespawnAttempts()) {
                attemptsMap.remove(uuid);
                followerUser.removeFollowerEntity();
            } else {
                if (attempts == 1) {
                    Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> attemptsMap.remove(uuid), 600);
                }
                attemptsMap.put(uuid, attempts + 1);
                followerUser.respawnFollowerEntity();
            }

            cancel(follower);
            return;
        }

        if (!player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), follower::kill, 5);
        }
    }

    @Override
    public int getPeriod() {
        return 1;
    }
}
