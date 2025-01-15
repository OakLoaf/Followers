package org.lushplugins.followers.utils.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.lushplugins.lushlib.utils.DisplayItemStack;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class AnvilMenu {
    private static final HashMap<UUID, AnvilMenu> menus = new HashMap<>();

    private final Inventory inventory;
    private final Function<String, Boolean> textPredicate;
    private final Consumer<String> onCompletion;
    private final Consumer<String> onCancel;
    private String input;

    private AnvilMenu(Player player, String prompt, String initialInput, Function<String, Boolean> textPredicate, Consumer<String> onCompletion, Consumer<String> onCancel) {
        this.inventory = Bukkit.createInventory(null, InventoryType.ANVIL, prompt);
        this.textPredicate = textPredicate;
        this.onCompletion = onCompletion;
        this.onCancel = onCancel;

        this.inventory.setItem(0, DisplayItemStack.builder(Material.PAPER)
            .setDisplayName(initialInput)
            .build()
            .asItemStack());

        updateInput(initialInput);

        player.openInventory(this.inventory);
        menus.put(player.getUniqueId(), this);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isInputValid() {
        return textPredicate.apply(input);
    }

    public void updateInput(String input) {
        this.input = input;

        DisplayItemStack.Builder builder = DisplayItemStack.builder();
        if (textPredicate.apply(input)) {
            builder
                .setType(Material.LIME_DYE)
                .setDisplayName("&a" + input);
        } else {
            builder
                .setType(Material.RED_DYE)
                .setDisplayName("&c" + input);
        }

        this.inventory.setItem(2, builder.build().asItemStack());
    }

    public void complete() {
        onCompletion.accept(input);
    }

    public void cancel() {
        onCancel.accept(input);
    }

    public static AnvilMenu getMenu(UUID uuid) {
        return menus.get(uuid);
    }

    public static void removeMenu(UUID uuid) {
        menus.remove(uuid);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String prompt = "Enter an input:";
        private String initialInput = "";
        private Function<String, Boolean> textPredicate = (input) -> true;
        private Consumer<String> onCompletion = (input) -> {};
        private Consumer<String> onCancel = (input) -> {};

        private Builder() {}

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder initialInput(String initialInput) {
            this.initialInput = initialInput;
            return this;
        }

        public Builder textPredicate(Function<String, Boolean> textPredicate) {
            this.textPredicate = textPredicate;
            return this;
        }

        public Builder onCompletion(Consumer<String> onCompletion) {
            this.onCompletion = onCompletion;
            return this;
        }

        public Builder onCancel(Consumer<String> onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public AnvilMenu open(Player player) {
            return new AnvilMenu(player, prompt, initialInput, textPredicate, onCompletion, onCancel);
        }
    }
}
