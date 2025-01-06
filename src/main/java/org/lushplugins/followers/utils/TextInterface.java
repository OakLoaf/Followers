package org.lushplugins.followers.utils;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.lushplugins.followers.Followers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class TextInterface {
    private final SignMenuFactory signFactory = new SignMenuFactory();
    private String originName = "";
    private List<String> title = new ArrayList<>();
    private String placeholder = "";
    private String inputName = "Input";
    private FloodgateFormFactory formFactory = null;

    public TextInterface originName(String originName) {
        this.originName = originName;
        return this;
    }

    public TextInterface title(String title) {
        this.title = Followers.getInstance().getConfigManager().getSignTitle();
        this.title.set(0, title);
        if (Followers.getInstance().hasFloodgate()) {
            formFactory = new FloodgateFormFactory();
        }

        return this;
    }

    public TextInterface title(List<String> title) {
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
        if (title == null || title.isEmpty()) {
            throw new IllegalStateException("Title is null! You must set a title to use this class");
        }

        if (Followers.getInstance().hasFloodgate() && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            formFactory.form(player, response, title.get(0), inputName, placeholder);
        } else {
            SignMenuFactory.Menu menu = signFactory.newMenu(Arrays.asList(originName, "^^^^^^^^^^^", title.get(0), title.get(1)));
            menu.reopenIfFail(true).response((ignored, output) -> {
                response.accept(output[0]);
                return true;
            });
            menu.open(player);
        }
    }
}