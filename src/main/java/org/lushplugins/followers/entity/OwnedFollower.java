package org.lushplugins.followers.entity;

import com.github.retrooper.packetevents.util.Vector3d;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.LivingEntity;

public class OwnedFollower extends Follower {
    private final LivingEntity owner;
    private boolean visible;

    public OwnedFollower(LivingEntity owner, String followerType) {
        super(followerType);
        this.owner = owner;
        this.visible = !owner.isInvisible();
    }

    public LivingEntity getOwner() {
        return owner;
    }

    @Override
    public Vector3d getTarget() {
        return SpigotConversionUtil.fromBukkitLocation(owner.getEyeLocation()).getPosition();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        refresh();
    }

    @Override
    public void refresh() {
        if (!visible) {
            if (this.isSpawned()) {
                despawn();
            }

            return;
        }

        super.refresh();
    }
}
