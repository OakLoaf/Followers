package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.lushplugins.lushlib.utils.RegistryUtils;

public class EntityTypeUtils {

    public static Material getSpawnEgg(EntityType entityType) {
        if (entityType.equals(EntityTypes.ARMOR_STAND)) {
            return Material.ARMOR_STAND;
        } else if (entityType.equals(EntityTypes.GIANT)) {
            return Material.ZOMBIE_SPAWN_EGG;
        } else {
            String materialRaw = entityType.getName().toString() + "_spawn_egg";

            try {
                return RegistryUtils.fromString(Registry.MATERIAL, materialRaw);
            } catch (IllegalArgumentException e) {
                return Material.POLAR_BEAR_SPAWN_EGG;
            }
        }
    }
}
