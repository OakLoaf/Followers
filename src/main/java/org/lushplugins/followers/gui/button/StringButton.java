package org.lushplugins.followers.gui.button;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.followers.utils.menu.TextInterface;
import org.lushplugins.lushlib.gui.button.ItemButton;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class StringButton extends ItemButton {
    private final Callable<ItemStack> item;
    private final Consumer<String> onCompletion;
    private String value;

    public StringButton(String value, Callable<ItemStack> item, String prompt, Function<String, Boolean> textPredicate, Consumer<String> onCompletion) {
        super((event) -> {});
        this.value = value;
        this.item = item;
        this.onCompletion = onCompletion;


        this.onClick((event) -> {
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            Inventory inventory = event.getClickedInventory();
            TextInterface.builder()
                .inputType(TextInterface.InputType.ANVIL)
                .prompt(prompt)
                .initialInput(this.value)
                .textPredicate(textPredicate)
                .onCompletion((input) -> {
                    this.value = input;
                    this.onCompletion.accept(input);

                    if (inventory != null) {
                        player.openInventory(inventory);
                    }
                })
                .onCancel((ignored) -> {
                    if (inventory != null) {
                        player.openInventory(inventory);
                    }
                })
                .open(player);
        });
    }

    @Override
    public ItemStack getItemStack(Player player) {
        try {
            return item.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
