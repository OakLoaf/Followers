package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import org.bukkit.World;

public class ParticleUtils {

    public static void spawnParticle(Particle<?> particle, World world, Vector3d position) {
        spawnParticle(particle, world, position, 1, new Vector3f(0f, 0f, 0f), 0f, false);
    }

    public static void spawnParticle(Particle<?> particle, World world, Vector3d position, int count, Vector3f offset, float speed, boolean longDistance) {
        WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
            particle, longDistance, position, offset, speed, count
        );

        world.getPlayers().forEach(player -> {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            if (user != null) {
                user.sendPacket(packet);
            }
        });
    }
}
