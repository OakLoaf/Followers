package org.lushplugins.followers.export;

import org.lushplugins.followers.Followers;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lushplugins.followers.config.FollowerHandler;
import org.lushplugins.followers.utils.ExtendedSimpleItemStack;
import org.lushplugins.followers.utils.entity.LivingEntityConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class GeyserSkullExporter {

    public void startExport() throws IOException {
        Followers.getInstance().saveResource("export/custom-skulls.yml", true);

        HashSet<String> textures = new HashSet<>();
        for (FollowerHandler followerHandler : Followers.getInstance().getFollowerManager().getFollowers().values()) {
            if (followerHandler.getEntityConfig() instanceof LivingEntityConfiguration entityConfig) {
                for (ExtendedSimpleItemStack item : entityConfig.getEquipment().values()) {
                    String skullTexture = item.getSkullTexture();
                    if (skullTexture != null) {
                        textures.add(skullTexture);
                    }
                }
            }
        }

        File exportFile = new File(Followers.getInstance().getDataFolder(), "export/custom-skulls.yml");
        YamlConfiguration customSkullsConfig = YamlConfiguration.loadConfiguration(exportFile);
        customSkullsConfig.set("player-profiles", textures.stream().toList());
        customSkullsConfig.save(exportFile);
    }
}
