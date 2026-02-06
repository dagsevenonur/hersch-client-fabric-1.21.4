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
            int bgColor = (a << 24); 
            // Removed border as requested
            int borderColor = 0; 
            
            drawSharpBox(ctx, 0, 0, boxW, boxH, bgColor, borderColor);
        }

        int tx = padding;
        int ty = padding;

        if (textShadow) ctx.drawTextWithShadow(mc.textRenderer, text, tx, ty, 0xFFFFFF);
        else ctx.drawText(mc.textRenderer, text, tx, ty, 0xFFFFFF, false);

        ctx.getMatrices().pop();
    }
    
    public static void drawSharpBox(DrawContext ctx, int x, int y, int w, int h, int color, int borderColor) {
        // Outline
        if (borderColor != 0) {
            ctx.fill(x, y, x + w, y + 1, borderColor);         // Top
            ctx.fill(x, y + h - 1, x + w, y + h, borderColor); // Bottom
            ctx.fill(x, y, x + 1, y + h, borderColor);         // Left
            ctx.fill(x + w - 1, y, x + w, y + h, borderColor); // Right
        }
        
        // Fill
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }
}
