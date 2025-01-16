package org.lushplugins.followers.utils.menu;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.lushplugins.followers.Followers;

import java.util.function.BiConsumer;

public class FloodgateFormFactory {

    public void form(Player player, BiConsumer<String, Player> response, String title, String inputName, String placeholder) {
        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            CustomForm form = CustomForm.builder()
                .title(title)
                .input(inputName, placeholder)
                .responseHandler((string) -> {
                    String output = string.length() >= 3 ? string.substring(2, string.length() - 3) : Followers.getInstance().getConfigManager().getDefaultNickname();
                    response.accept(output, player);
                })
                .build();

            FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
        }
    }
}
