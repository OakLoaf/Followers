package org.lushplugins.followers.utils;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.lushplugins.lushlib.registry.RegistryUtils;

public class EntityTypeUtils {

    public static Material getSpawnEgg(EntityType entityType) {
        if (entityType.equals(EntityTypes.ARMOR_STAND)) {
            return Material.ARMOR_STAND;
        } else if (entityType.equals(EntityTypes.GIANT)) {
            return Material.ZOMBIE_SPAWN_EGG;
        } else if (entityType.equals(EntityTypes.PLAYER)) {
            return Material.PLAYER_HEAD;
        } else {
            String materialRaw = entityType.getName().toString() + "_spawn_egg";

            Material material = RegistryUtils.parseString(materialRaw, Registry.MATERIAL);
            return material != null ? material : Material.POLAR_BEAR_SPAWN_EGG;
        }
    }
}
