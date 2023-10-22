package me.dave.followers.export;

import me.dave.followers.Followers;
import org.bukkit.configuration.file.YamlConfiguration;

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

        Followers.followerManager.getFollowers().values().forEach(followerHandler -> {
            List<String> followerTextures = List.of(
                    Followers.getSkullCreator().getB64(followerHandler.getHead()),
                    Followers.getSkullCreator().getB64(followerHandler.getChest()),
                    Followers.getSkullCreator().getB64(followerHandler.getLegs()),
                    Followers.getSkullCreator().getB64(followerHandler.getFeet()),
                    Followers.getSkullCreator().getB64(followerHandler.getMainHand()),
                    Followers.getSkullCreator().getB64(followerHandler.getOffHand())
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
}
