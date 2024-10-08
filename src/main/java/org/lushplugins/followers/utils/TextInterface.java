package org.lushplugins.followers.utils;

import org.lushplugins.followers.Followers;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Arrays;
import java.util.function.Consumer;

public class TextInterface {
    private String title = "";
    private String placeholder = "";

    private String inputName = "Input";
    private final SignMenuFactory signFactory = new SignMenuFactory();
    private FloodgateFormFactory formFactory = null;

    public TextInterface title(String title) {
        this.title = title;

        if (Followers.getInstance().hasFloodgate()) {
            formFactory = new FloodgateFormFactory();
        }

        return this;
    }

    public TextInterface placeholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public TextInterface inputName(String inputName) {
        this.inputName = inputName;
        return this;
    }

    public void getInput(Player player, Consumer<String> response) {
        if (title == null) {
            throw new IllegalStateException("Title is null! You must set a title to use this class");
        }

        if (Followers.getInstance().hasFloodgate() && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            formFactory.form(player, response, title, inputName, placeholder);
        } else {
            SignMenuFactory.Menu menu = signFactory.newMenu(Arrays.asList("", "^^^^^^^^^^^", title, ""));
            menu.reopenIfFail(true).response((ignored, output) -> {response.accept(output[0]); return true;});
            menu.open(player);
        }
    }
}