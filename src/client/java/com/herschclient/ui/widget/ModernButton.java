package com.herschclient.ui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

public abstract class ModernButton extends PressableWidget {

    public interface Action {
        void run();
    }

    private final Action action;

    public ModernButton(int x, int y, int w, int h, Text message, Action action) {
        super(x, y, w, h, message);
        this.action = action;
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovered();

        // Modern flat renkler
        int bg = hovered ? 0xCC2A2A2A : 0xCC1E1E1E;   // hafif açılan hover
        int border = hovered ? 0xFF6A6A6A : 0xFF3A3A3A;

        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;

        // arka plan
        ctx.fill(x, y, x + w, y + h, bg);

        // border (ince çerçeve)
        ctx.fill(x, y, x + w, y + 1, border);             // top
        ctx.fill(x, y + h - 1, x + w, y + h, border);     // bottom
        ctx.fill(x, y, x + 1, y + h, border);             // left
        ctx.fill(x + w - 1, y, x + w, y + h, border);     // right

        // text (ortalanmış)
        MinecraftClient mc = MinecraftClient.getInstance();
        int textColor = hovered ? 0xFFFFFFFF : 0xFFE0E0E0;
        ctx.drawCenteredTextWithShadow(
                mc.textRenderer,
                this.getMessage(),
                x + w / 2,
                y + (h - 8) / 2,
                textColor
        );
    }
}
