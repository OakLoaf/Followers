package me.dave.followers.entity.tasks;

import me.dave.followers.Followers;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ValidateTask extends AbstractTask {
    private static final HashMap<UUID, Integer> attemptsMap = new HashMap<>();
    private final Player player;

    public ValidateTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
    }

    @Override
    public void tick() {
        if (followerEntity.getBodyArmorStand() == null || !followerEntity.getBodyArmorStand().isValid()) {
            FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
            UUID uuid = player.getUniqueId();

            int attempts = attemptsMap.getOrDefault(uuid, 0);
            if (attempts >= Followers.configManager.getMaxRespawnAttempts()) {
                attemptsMap.remove(uuid);
                followerUser.removeFollowerEntity();
            } else {
                if (attempts == 1) {
                    Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> attemptsMap.remove(uuid), 600);
                }
                attemptsMap.put(uuid, attempts + 1);
                followerUser.respawnFollowerEntity();
            }

            cancel();
            return;
        }

        if (!player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), followerEntity::kill, 5);
        }
    }

    @Override
    public String getIdentifier() {
        return "validate";
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public int getPeriod() {
        return 1;
    }
}
