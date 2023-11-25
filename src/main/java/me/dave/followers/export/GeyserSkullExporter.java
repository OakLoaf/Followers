package me.dave.followers.export;

import me.dave.followers.Followers;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
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

        Followers.followerManager.getFollowers().values().forEach(followerHandler -> {
            List<String> followerTextures = List.of(
                    getNonNullB64(followerHandler.getHead()),
                    getNonNullB64(followerHandler.getChest()),
                    getNonNullB64(followerHandler.getLegs()),
                    getNonNullB64(followerHandler.getFeet()),
                    getNonNullB64(followerHandler.getMainHand()),
                    getNonNullB64(followerHandler.getOffHand())
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
    private String getNonNullB64(ItemStack itemStack) {
        String b64 = Followers.getSkullCreator().getB64(itemStack);
        return b64 != null ? b64 : "";
    }
}
