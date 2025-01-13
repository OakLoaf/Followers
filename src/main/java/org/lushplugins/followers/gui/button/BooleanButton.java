package org.lushplugins.followers.gui.button;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.lushlib.LushLib;
import org.lushplugins.lushlib.gui.button.ItemButton;
import org.lushplugins.lushlib.gui.inventory.Gui;
import org.lushplugins.lushlib.manager.GuiManager;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class BooleanButton extends ItemButton {
    private final Callable<ItemStack> ifTrue;
    private final Callable<ItemStack> ifFalse;
    private final Consumer<Boolean> linkValue;
    private boolean value;

    public BooleanButton(boolean value, Callable<ItemStack> ifTrue, Callable<ItemStack> ifFalse, Consumer<Boolean> linkValue) {
        super((event) -> {});
        this.value = value;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        this.linkValue = linkValue;

        this.onClick((event) -> {
            this.value = !this.value;
            this.linkValue.accept(this.value);

            LushLib.getInstance().getPlugin().getManager(GuiManager.class).ifPresent((manager) -> {
                Gui gui = manager.getGui(event.getWhoClicked().getUniqueId());
                if (gui != null && event.getInventory().equals(gui.getInventory())) {
                    gui.refresh(event.getRawSlot());
                }
            });
        });
    }

    @Override
    public ItemStack getItemStack(Player player) {
        try {
            return value ? ifTrue.call() : ifFalse.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
