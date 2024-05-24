package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;

public class LocationUtil {

    public static Location add(Location location, Vector3d vector) {
        location.setPosition(location.getPosition().add(vector));
        return location;
    }
}
