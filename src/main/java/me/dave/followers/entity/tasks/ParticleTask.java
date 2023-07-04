package me.dave.followers.entity.tasks;

import me.dave.followers.entity.FollowerEntity;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;

public class ParticleTask extends AbstractTask {
    private final ArmorStand armorStand;
    private final Particle particle;

    public ParticleTask(FollowerEntity followerEntity, Particle particle) {
        super(followerEntity);
        this.armorStand = followerEntity.getBodyArmorStand();
        this.particle = particle;
    }

    @Override
    public FollowerTaskType getType() {
        return FollowerTaskType.PARTICLE;
    }

    @Override
    public void run() {
        if (checkCancel()) return;

        if (armorStand == null || !armorStand.isValid()) {
            cancel();
            return;
        }
        armorStand.getWorld().spawnParticle(particle, armorStand.getLocation().add(0, -0.15, 0), 1, 0, 0, 0, 0);
    }
}
