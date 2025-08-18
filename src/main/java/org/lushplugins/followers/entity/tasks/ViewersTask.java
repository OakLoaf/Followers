package org.lushplugins.followers.entity.tasks;

import com.github.retrooper.packetevents.protocol.world.Location;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.utils.LocationUtils;

import java.util.Set;
import java.util.UUID;

public class ViewersTask extends FollowerTask {
    private static final int VISIBILITY_DISTANCE = 32;

    public ViewersTask(String id) {
        super(id);
    }

    @Override
    public void tick(Follower follower) {
        Location location = follower.getLocation();
        WrapperEntity entity = follower.getEntity();
        if (entity == null) {
            return;
        }

        Set<UUID> viewers = entity.getViewers();
        if (location == null) {
            viewers.forEach(entity::removeViewer);
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!player.getWorld().equals(follower.getWorld()) && viewers.contains(uuid)) {
                entity.removeViewer(uuid);
                continue;
            }

            Location playerLocation = SpigotConversionUtil.fromBukkitLocation(player.getLocation());
            double distance = LocationUtils.getDistance(playerLocation, location);
            boolean inRange = distance <= VISIBILITY_DISTANCE;

            if (inRange && !viewers.contains(uuid)) {
                entity.addViewer(uuid);

                WrapperEntity nameTagEntity = follower.getNameTagEntity();
                if (nameTagEntity != null && nameTagEntity.isSpawned()) {
                    nameTagEntity.addViewer(uuid);
                }

                // TODO: Remove on EntityLib implementation
                entity.refresh();
            } else if (!inRange && viewers.contains(uuid)) {
                WrapperEntity nameTagEntity = follower.getNameTagEntity();
                if (nameTagEntity != null && nameTagEntity.isSpawned()) {
                    nameTagEntity.removeViewer(uuid);
                }

                entity.removeViewer(uuid);
            }
        }
    }

    @Override
    public int getPeriod() {
        return 20;
    }
}
