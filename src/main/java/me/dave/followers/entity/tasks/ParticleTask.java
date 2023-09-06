package me.dave.followers.entity.tasks;

import me.dave.followers.entity.FollowerEntity;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;

public class ParticleTask extends FollowerTask {
    public static final String ID = "particle";
    private final ArmorStand armorStand;
    private final Particle particle;

    public ParticleTask(FollowerEntity followerEntity, Particle particle) {
        super(followerEntity);
        this.armorStand = followerEntity.getBodyEntity();
        this.particle = particle;
    }

    @Override
    public void tick() {
        if (!followerEntity.isBodyEntityValid()) {
            cancel();
            return;
        }

        armorStand.getWorld().spawnParticle(particle, armorStand.getLocation().add(0, -0.15, 0), 1, 0, 0, 0, 0);
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public int getPeriod() {
        return 3;
    }
}
