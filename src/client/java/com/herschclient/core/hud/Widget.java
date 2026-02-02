package com.herschclient.core.hud;

import net.minecraft.client.gui.DrawContext;

public abstract class Widget {
    private int x;
    private int y;
    private boolean visible = true;

    protected Widget(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final int getX() { return x; }
    public final int getY() { return y; }
    public final void setPos(int x, int y) { this.x = x; this.y = y; }

    public final boolean isVisible() { return visible; }
    public final void setVisible(boolean visible) { this.visible = visible; }

    public abstract String getId(); // config için sabit id
    public abstract void render(DrawContext ctx);
}
