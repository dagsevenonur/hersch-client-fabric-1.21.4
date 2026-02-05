package com.herschclient.ui;

import com.herschclient.HerschClient;
import com.herschclient.core.hud.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class HudEditScreen extends Screen {

    private final Screen parent;

    private Widget dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditScreen(Screen parent) {
        super(Text.literal("HUD Düzenle"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        // 1) Grid & Guides
        drawGrid(ctx);
        drawGuides(ctx);

        // 2) HUD Render (actual widgets)
        HerschClient.HUD.render(ctx);

        // 3) Instructions
        ctx.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("§lHUD EDIT MODE"),
                width / 2, 10, 0xFFFFFFFF
        );
        ctx.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("§7Drag widgets to move. Press ESC to save."),
                width / 2, 22, 0xFFAAAAAA
        );

        // 4) Outlines & Interaction
        MinecraftClient mc = MinecraftClient.getInstance();

        for (Widget w : HerschClient.HUD.getWidgets()) {
            if (!w.isEnabled()) continue;

            int x = w.getX();
            int y = w.getY();
            int ww = Math.max(10, w.getWidth(mc));
            int hh = Math.max(10, w.getHeight(mc));

            boolean hover = (mouseX >= x && mouseX <= x + ww && mouseY >= y && mouseY <= y + hh);
            boolean isDragging = (dragging == w);

            // Colors
            int borderColor = 0x40FFFFFF; // Faint white
            int bgColor = 0x00000000;

            if (hover || isDragging) {
                borderColor = 0xFF3B72FF; // Hersch Blue
                bgColor = 0x203B72FF;     // Semi-transparent blue fill
            }

            // Draw box
            ctx.fill(x, y, x + ww, y + hh, bgColor);
            
            // Border (using simple logic for now, could use UiDraw)
            ctx.fill(x - 1, y - 1, x + ww + 1, y, borderColor);          // Top
            ctx.fill(x - 1, y + hh, x + ww + 1, y + hh + 1, borderColor);// Bottom
            ctx.fill(x - 1, y, x, y + hh, borderColor);                  // Left
            ctx.fill(x + ww, y, x + ww + 1, y + hh, borderColor);        // Right

            // Coordinates on drag
            if (isDragging || hover) {
                String coords = String.format("(%d, %d)", x, y);
                int cw = textRenderer.getWidth(coords);
                
                // Draw above or below depending on room
                int ty = y - 12;
                if (ty < 0) ty = y + hh + 4;
                
                int tx = x + (ww - cw) / 2;
                ctx.drawTextWithShadow(textRenderer, coords, tx, ty, 0xFFE0E0E0);
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawGrid(DrawContext ctx) {
        int gridSize = 20;
        int color = 0x15FFFFFF; // Very faint white

        for (int x = 0; x < width; x += gridSize) {
            ctx.fill(x, 0, x + 1, height, color);
        }
        for (int y = 0; y < height; y += gridSize) {
            ctx.fill(0, y, width, y + 1, color);
        }
    }

    private void drawGuides(DrawContext ctx) {
        int cx = width / 2;
        int cy = height / 2;
        int color = 0x40FF0000; // Semi-transparent red

        // Vertical guide
        ctx.fill(cx - 1, 0, cx + 1, height, color);
        // Horizontal guide
        ctx.fill(0, cy - 1, width, cy + 1, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        MinecraftClient mc = MinecraftClient.getInstance();

        // üstteki daha öncelikli olsun diye tersten gezmek daha iyi:
        var list = HerschClient.HUD.getWidgets();
        for (int i = list.size() - 1; i >= 0; i--) {
            Widget w = list.get(i);
            if (!w.isEnabled()) continue;

            int x = w.getX();
            int y = w.getY();
            int ww = Math.max(10, w.getWidth(mc));
            int hh = Math.max(10, w.getHeight(mc));

            if (mouseX >= x && mouseX <= x + ww && mouseY >= y && mouseY <= y + hh) {
                dragging = w;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (button == 0 && dragging != null) {
            dragging.setPos((int) mouseX - dragOffsetX, (int) mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC
        if (keyCode == 256) {
            this.client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
