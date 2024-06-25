package org.lushplugins.followers.entity.tasks;

import com.github.retrooper.packetevents.protocol.world.Location;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.bukkit.entity.Player;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.utils.LocationUtils;

import java.util.UUID;

public class ViewersTask extends FollowerTask {
    private static final int VISIBILITY_DISTANCE = 32;

    public ViewersTask(String id) {
        super(id);
    }

    @Override
    public void tick(Follower follower) {
        for (Player player : follower.getWorld().getPlayers()) {
            UUID uuid = player.getUniqueId();
            Location location = follower.getLocation();
            WrapperLivingEntity entity = follower.getEntity();
            if (entity == null) {
                continue;
            }

            if (location == null) {
                entity.removeViewer(uuid);
                continue;
            }

            Location playerLocation = SpigotConversionUtil.fromBukkitLocation(player.getLocation());
            double distance = LocationUtils.getDistance(playerLocation, location);
            boolean inRange = distance <= VISIBILITY_DISTANCE;

            if (inRange && !entity.getViewers().contains(uuid)) {
                entity.addViewer(uuid);

                WrapperEntity nameTagEntity = follower.getNametagEntity();
                if (nameTagEntity != null && nameTagEntity.isSpawned()) {
                    nameTagEntity.addViewer(uuid);
                }

                // TODO: Remove on EntityLib implementation
                entity.refresh();
            } else if (!inRange && entity.getViewers().contains(uuid)) {
                WrapperEntity nameTagEntity = follower.getNametagEntity();
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
