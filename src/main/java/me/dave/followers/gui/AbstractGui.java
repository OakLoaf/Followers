package me.dave.followers.gui;

public abstract class AbstractGui {

    public abstract String getType();
    public abstract void recalculateContents();

    public abstract void openInventory();
}
