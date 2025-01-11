package org.lushplugins.followers.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.lushlib.utils.DisplayItemStack;
import org.lushplugins.lushlib.utils.SkullCreator;

import java.util.UUID;

public class YamlConverter {

    public static DisplayItemStack.Builder getDisplayItemBuilder(ConfigurationSection section) {
        DisplayItemStack.Builder builder = org.lushplugins.lushlib.utils.converter.YamlConverter.getDisplayItemBuilder(section);

        if (section.contains("name")) {
            builder.setDisplayName(section.getString("name"));
        }

        if (section.contains("customModelData")) {
            builder.setCustomModelData(section.getInt("customModelData"));
        }

        if (section.contains("skullType") && section.contains("texture")) {
            if (section.getString("skullType").equalsIgnoreCase("custom")) {
                builder.setSkullTexture(section.getString("texture"));
            } else {
                UUID uuid = UUID.fromString(section.getString("uuid"));
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                builder.setSkullTexture(SkullCreator.getTexture(player));
            }
        }

        if (section.contains("skull-texture")) {
            builder.setSkullTexture(section.getString("skull-texture"));
        }

        return builder;
    }

    public static DisplayItemStack getDisplayItem(ConfigurationSection section) {
        return getDisplayItemBuilder(section).build();
    }
}
