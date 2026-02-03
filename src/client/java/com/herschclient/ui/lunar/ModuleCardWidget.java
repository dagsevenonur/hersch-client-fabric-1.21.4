package com.herschclient.ui.lunar;

import com.herschclient.core.hud.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public abstract class ModuleCardWidget extends ClickableWidget {

    private final Widget widget;
    private final Runnable openOptions; // şimdilik boş bırakabiliriz

    public ModuleCardWidget(int x, int y, int w, int h, Widget widget, Runnable openOptions) {
        super(x, y, w, h, Text.empty());
        this.widget = widget;
        this.openOptions = openOptions;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        boolean hover = isHovered();
        int x = getX(), y = getY();

        UiDraw.roundedRect(ctx, x, y, width, height, 8, LunarColors.CARD_BG);
        if (hover) {
            // hafif outline
            ctx.fill(x, y, x + width, y + 1, LunarColors.CARD_OUTLINE);
            ctx.fill(x, y + height - 1, x + width, y + height, LunarColors.CARD_OUTLINE);
            ctx.fill(x, y, x + 1, y + height, LunarColors.CARD_OUTLINE);
            ctx.fill(x + width - 1, y, x + width, y + height, LunarColors.CARD_OUTLINE);
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        // Icon placeholder
        UiDraw.roundedRect(ctx, x + 12, y + 12, 34, 34, 8, 0xFF303030);

        // Name
        ctx.drawTextWithShadow(mc.textRenderer, widget.getName(), x + 54, y + 16, LunarColors.TEXT_MAIN);

        // OPTIONS bar
        UiDraw.roundedRect(ctx, x + 12, y + height - 34, width - 24, 18, 6, 0xFF2B2B2B);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal("OPTIONS"), x + width / 2, y + height - 30, LunarColors.TEXT_DIM);

        // Enabled pill
        int pillW = width - 24;
        int pillX = x + 12;
        int pillY = y + height - 14;
        int pillColor = widget.isEnabled() ? LunarColors.GREEN_ON : 0xFF3A3A3A;
        UiDraw.roundedRect(ctx, pillX, pillY, pillW, 12, 6, pillColor);

        String pillText = widget.isEnabled() ? "ENABLED" : "DISABLED";
        int textColor = widget.isEnabled() ? 0xFFFFFFFF : LunarColors.TEXT_DIM;
        ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(pillText), x + width / 2, pillY + 2, textColor);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // alt pill kısmına tıklayınca toggle
        int x = getX(), y = getY();
        int pillX = x + 12;
        int pillY = y + height - 14;
        int pillW = width - 24;
        int pillH = 12;

        if (UiDraw.isInside((int)mouseX, (int)mouseY, pillX, pillY, pillW, pillH)) {
            widget.setEnabled(!widget.isEnabled());
            return;
        }

        // options bar’a tık (şimdilik)
        int optX = x + 12;
        int optY = y + height - 34;
        int optW = width - 24;
        int optH = 18;
        if (UiDraw.isInside((int)mouseX, (int)mouseY, optX, optY, optW, optH)) {
            openOptions.run();
        }
    }
}
