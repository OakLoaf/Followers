package org.lushplugins.followers.entity.tasks;

import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.bukkit.World;
import org.lushplugins.followers.entity.Follower;
import org.lushplugins.followers.utils.LocationUtils;
import org.lushplugins.followers.utils.ParticleUtils;

public class ParticleTask extends FollowerTask {
    private final ParticleType<?> particle;

    public ParticleTask(String id, ParticleType<?> particle) {
        super(id);
        this.particle = particle;
    }

    @Override
    public void tick(Follower follower) {
        WrapperLivingEntity entity = follower.getEntity();
        if (entity == null || !follower.isSpawned()) {
            follower.removeTask(this.getId());
            return;
        }

        World world = follower.getWorld();
        ParticleUtils.spawnParticle(
            new Particle<>(particle),
            world,
            LocationUtils.add(entity.getLocation(), new Vector3d(0, -0.15, 0)),
            1,
            Vector3f.zero(),
            0,
            false
        );
    }

    @Override
    public int getPeriod() {
        return 3;
    }
}
