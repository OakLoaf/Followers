package org.lushplugins.followers.export;

import org.lushplugins.followers.Followers;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class GeyserSkullExporter {

    public void startExport() throws IOException {
        Followers.getInstance().saveResource("export/custom-skulls.yml", true);

        File exportFile = new File(Followers.getInstance().getDataFolder(), "export/custom-skulls.yml");
        YamlConfiguration customSkullsConfig = YamlConfiguration.loadConfiguration(exportFile);

        HashSet<String> textures = new HashSet<>();

        Followers.getInstance().getFollowerManager().getFollowers().values().forEach(followerHandler -> {
            List<String> followerTextures = List.of(
                    getNonNullString(followerHandler.getHead().getSkullTexture()),
                    getNonNullString(followerHandler.getChest().getSkullTexture()),
                    getNonNullString(followerHandler.getLegs().getSkullTexture()),
                    getNonNullString(followerHandler.getFeet().getSkullTexture()),
                    getNonNullString(followerHandler.getMainHand().getSkullTexture()),
                    getNonNullString(followerHandler.getOffHand().getSkullTexture())
            );

            followerTextures.forEach(texture -> {
                if (texture != null && !texture.isBlank()) {
                    textures.add(texture);
                }
            });
        });

        customSkullsConfig.set("player-profiles", textures.stream().toList());

        customSkullsConfig.save(exportFile);
    }

    @NotNull
    private String getNonNullString(String string) {
        return string != null ? string : "";
    }
}
