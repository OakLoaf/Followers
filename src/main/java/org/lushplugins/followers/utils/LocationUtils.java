package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;

public class LocationUtils {

    public static Location add(Location location, Vector3d vector) {
        location.setPosition(location.getPosition().add(vector));
        return location;
    }

    public static double getDistance(Location location1, Location location2) {
        return Math.sqrt(Math.pow(location1.getX() - location2.getX(), 2)
            + Math.pow(location1.getY() - location2.getY(), 2)
            + Math.pow(location1.getZ() - location2.getZ(), 2));
    }
}
