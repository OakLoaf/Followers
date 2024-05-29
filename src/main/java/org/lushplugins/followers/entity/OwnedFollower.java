package org.lushplugins.followers.entity;

import com.github.retrooper.packetevents.util.Vector3d;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

public class OwnedFollower extends Follower {
    private final LivingEntity owner;

    public OwnedFollower(LivingEntity owner, String followerType) {
        super(followerType);
        this.owner = owner;
        setVisible(!owner.isInvisible());
    }

    public LivingEntity getOwner() {
        return owner;
    }

    @Override
    public World getWorld() {
        return owner.getWorld();
    }

    @Override
    public Vector3d getTarget() {
        return SpigotConversionUtil.fromBukkitLocation(owner.getEyeLocation()).getPosition();
    }
}
