package me.dave.followers.utils;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.function.Consumer;

public class FloodgateFormFactory {

    public void form(Player player, Consumer<String> response, String title, String inputName, String placeholder) {
        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            CustomForm form = CustomForm.builder().title(title).input(inputName, placeholder).responseHandler((string) -> response.accept(string.length() >= 3 ? string.substring(2, string.length() - 3) : "Unnamed")).build();
            FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
        }
    }
}
