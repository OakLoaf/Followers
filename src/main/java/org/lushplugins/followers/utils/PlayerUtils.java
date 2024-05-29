package org.lushplugins.followers.utils;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

public class PlayerUtils {

    public static boolean isVanished(@NotNull Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true;
            }
        }

        return false;
    }
}
