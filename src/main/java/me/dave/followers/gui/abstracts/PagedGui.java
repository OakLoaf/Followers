package me.dave.followers.gui.abstracts;

import org.bukkit.entity.Player;

public abstract class PagedGui extends AbstractGui {
    protected int page = 1;

    public PagedGui(int size, String title, Player player) {
        super(size, title, player);
    }

    public void setPage(int page) {
        this.page = page;
        recalculateContents();
    }

    public void nextPage() {
        setPage(++page);
    }

    public void previousPage() {
        setPage(--page);
    }
}
