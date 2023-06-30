package me.dave.followers.gui.abstracts;

public abstract class PagedGui extends AbstractGui {
    protected int page = 1;

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
