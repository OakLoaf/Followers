package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import org.bukkit.World;

public class ParticleUtils {

    public static void spawnParticle(Particle<?> particle, World world, Location location) {
        spawnParticle(particle, world, location, 1, new Vector3f(0f, 0f, 0f), 0f, false);
    }

    public static void spawnParticle(Particle<?> particle, World world, Location location, int count, Vector3f offset, float speed, boolean longDistance) {
        WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
            particle, longDistance, location.getPosition(), offset, speed, count
        );

        ProtocolManager protocolManager = PacketEvents.getAPI().getProtocolManager();
        world.getPlayers().forEach(player -> protocolManager.sendPacket(player, packet));
    }
}
