package com.herschclient.core.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public abstract class Widget {
    protected String name;
    protected boolean enabled = true;
    protected int x, y;

    public Widget(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPos(int x, int y) { this.x = x; this.y = y; }

    public int getWidth(MinecraftClient mc) {
        return mc.textRenderer.getWidth(this.name);
    }

    public int getHeight(MinecraftClient mc) {
        return 10;
    }

    /** Kartlarda gözükecek ikon. Override et. */
    public Identifier getIcon() { return null; }

    /** İkon PNG’inin gerçek boyutu (ör: 64x64). Override gerekmez genelde. */
    public int getIconTextureSize() { return 64; }

    public abstract void render(DrawContext ctx);
}
