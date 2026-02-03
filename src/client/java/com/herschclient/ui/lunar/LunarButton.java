package com.herschclient.ui.lunar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public abstract class LunarButton extends ClickableWidget {

    private final Runnable onClick;
    private final int radius;
    private final int bg;
    private final int bgHover;

    public LunarButton(int x, int y, int w, int h, Text msg, Runnable onClick, int radius, int bg, int bgHover) {
        super(x, y, w, h, msg);
        this.onClick = onClick;
        this.radius = radius;
        this.bg = bg;
        this.bgHover = bgHover;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        boolean hover = isHovered();
        UiDraw.roundedRect(ctx, getX(), getY(), width, height, radius, hover ? bgHover : bg);

        MinecraftClient mc = MinecraftClient.getInstance();
        int tx = getX() + width / 2;
        int ty = getY() + (height - 8) / 2;
        ctx.drawCenteredTextWithShadow(mc.textRenderer, getMessage(), tx, ty, LunarColors.TEXT_MAIN);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active) onClick.run();
    }
}
