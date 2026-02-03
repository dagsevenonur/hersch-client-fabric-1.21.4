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

        // HUD'ı normal şekilde çiz
        HerschClient.HUD.render(ctx);

        ctx.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Sürükle-bırak ile HUD konumlarını ayarla. ESC ile geri dön."),
                width / 2, 10, 0xFFFFFF
        );

        // Aktif widget'lara outline çiz
        MinecraftClient mc = MinecraftClient.getInstance();

        for (Widget w : HerschClient.HUD.getWidgets()) {
            if (!w.isEnabled()) continue;

            int x = w.getX();
            int y = w.getY();
            int ww = Math.max(10, w.getWidth(mc));
            int hh = Math.max(10, w.getHeight(mc));

            // basit çerçeve
            ctx.fill(x - 2, y - 2, x + ww + 2, y - 1, 0x80FFFFFF);
            ctx.fill(x - 2, y + hh + 1, x + ww + 2, y + hh + 2, 0x80FFFFFF);
            ctx.fill(x - 2, y - 2, x - 1, y + hh + 2, 0x80FFFFFF);
            ctx.fill(x + ww + 1, y - 2, x + ww + 2, y + hh + 2, 0x80FFFFFF);
        }

        super.render(ctx, mouseX, mouseY, delta);
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
