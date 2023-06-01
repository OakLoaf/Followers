package me.dave.followers.entity;

import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {
    private final ArmorStand armorStand;
    private final Particle particle;

    public ParticleTask(FollowerEntity follower, Particle particle) {
        this.armorStand = follower.bodyArmorStand;
        this.particle = particle;
    }

    @Override
    public void run() {
        if (armorStand == null || !armorStand.isValid()) {
            cancel();
            return;
        }
        armorStand.getWorld().spawnParticle(particle, armorStand.getLocation().add(0, -0.15, 0), 1, 0, 0, 0, 0);
    }
}
