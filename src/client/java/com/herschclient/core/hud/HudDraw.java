package com.herschclient.core.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class HudDraw {
    private HudDraw() {}

    public static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void drawBoxedText(
            DrawContext ctx,
            int x, int y,
            float scale,
            boolean background,
            float bgOpacity,
            int padding,
            boolean textShadow,
            String text
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int textW = mc.textRenderer.getWidth(text);
        int textH = mc.textRenderer.fontHeight;

        int boxW = textW + padding * 2;
        int boxH = textH + padding * 2;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(scale, scale, 1.0f);

        if (background) {
            int a = Math.round(bgOpacity * 255.0f);
            ctx.fill(0, 0, boxW, boxH, (a << 24)); // siyah alpha
        }

        int tx = padding;
        int ty = padding;

        if (textShadow) ctx.drawTextWithShadow(mc.textRenderer, text, tx, ty, 0xFFFFFF);
        else ctx.drawText(mc.textRenderer, text, tx, ty, 0xFFFFFF, false);

        ctx.getMatrices().pop();
    }
}
