package me.dave.followers.entity.tasks;

import me.dave.followers.Followers;
import me.dave.followers.data.FollowerUser;
import me.dave.followers.entity.FollowerEntity;
import me.dave.followers.api.events.PlayerVisiblityChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VisibilityTask extends AbstractTask {
    private final Player player;
    private boolean visible;

    public VisibilityTask(FollowerEntity followerEntity) {
        super(followerEntity);
        this.player = followerEntity.getPlayer();
        this.visible = !player.isInvisible() && !Followers.dataManager.getFollowerUser(player).isVanished();
    }

    @Override
    public void tick() {
        FollowerUser followerUser = Followers.dataManager.getFollowerUser(player);
        boolean visible = !player.isInvisible() && !followerUser.isVanished();

        if (this.visible != visible) {
            Bukkit.getPluginManager().callEvent(new PlayerVisiblityChangeEvent(player, this.visible, visible));
        }

        this.visible = visible;
    }

    @Override
    public String getIdentifier() {
        return "visibility";
    }

    @Override
    public int getDelay() {
        return 5;
    }

    @Override
    public int getPeriod() {
        return 20;
    }
}
