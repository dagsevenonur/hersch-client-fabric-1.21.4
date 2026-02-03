package com.herschclient.ui;

import com.herschclient.core.hud.Widget;
import com.herschclient.core.settings.Setting;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class WidgetOptionsScreen extends Screen {

    private final Screen parent;
    private final Widget widget;

    // Panel
    private int panelX, panelY, panelW, panelH;
    private final int headerH = 24;
    private final int pad = 10;

    // Satırlar (scroll yok şimdilik; gerekirse ekleriz)
    private final List<Row> rows = new ArrayList<>();

    public WidgetOptionsScreen(Screen parent, Widget widget) {
        super(Text.literal(widget.getName() + " Options"));
        this.parent = parent;
        this.widget = widget;
    }

    @Override
    protected void init() {
        panelW = Math.min(360, this.width - 40);
        panelH = Math.min(240, this.height - 40);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        rows.clear();

        int x = panelX + pad;
        int y = panelY + headerH + pad;

        int rowH = 22;
        int rowW = panelW - pad * 2;

        for (Setting<?> s : widget.getSettings()) {
            rows.add(new Row(x, y, rowW, rowH, s));
            y += rowH + 6;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }

    // Blur istemiyoruz
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // nothing
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Panel bg
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xB0121212);
        border(ctx, panelX, panelY, panelW, panelH, 0xFF2B2B2B);

        // Header
        ctx.fill(panelX, panelY, panelX + panelW, panelY + headerH, 0xCC1A1A1A);
        ctx.drawTextWithShadow(textRenderer, widget.getName().toUpperCase() + "  /  OPTIONS", panelX + 10, panelY + 7, 0xFFECECEC);

        // Close X
        int closeSize = 14;
        int closeX = panelX + panelW - closeSize - 8;
        int closeY = panelY + 5;
        boolean closeHover = inRect(mouseX, mouseY, closeX, closeY, closeSize, closeSize);
        ctx.fill(closeX, closeY, closeX + closeSize, closeY + closeSize, closeHover ? 0xFF3A3A3A : 0xFF2A2A2A);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("x"), closeX + closeSize / 2, closeY + 3, 0xFFFFFFFF);

        // Rows
        for (Row r : rows) {
            r.render(ctx, mouseX, mouseY, textRenderer);
        }

        // Back (alt)
        int backW = 90;
        int backH = 16;
        int backX = panelX + panelW - backW - 10;
        int backY = panelY + panelH - backH - 8;
        boolean backHover = inRect(mouseX, mouseY, backX, backY, backW, backH);
        ctx.fill(backX, backY, backX + backW, backY + backH, backHover ? 0xFF2A2A2A : 0xFF202020);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("BACK"), backX + backW / 2, backY + 4, 0xFFDADADA);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // close X
        int closeSize = 14;
        int closeX = panelX + panelW - closeSize - 8;
        int closeY = panelY + 5;
        if (inRect(mx, my, closeX, closeY, closeSize, closeSize)) {
            close();
            return true;
        }

        // rows click
        for (Row r : rows) {
            if (r.mouseClicked(mx, my, button)) return true;
        }

        // back
        int backW = 90;
        int backH = 16;
        int backX = panelX + panelW - backW - 10;
        int backY = panelY + panelH - backH - 8;
        if (inRect(mx, my, backX, backY, backW, backH)) {
            close();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ---------------- UI Row ----------------

    private static final class Row {
        final int x, y, w, h;
        final Setting<?> setting;

        Row(int x, int y, int w, int h, Setting<?> setting) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.setting = setting;
        }

        void render(DrawContext ctx, int mx, int my, net.minecraft.client.font.TextRenderer tr) {
            boolean hover = inRect(mx, my, x, y, w, h);
            ctx.fill(x, y, x + w, y + h, hover ? 0xFF1F1F1F : 0xFF191919);
            border(ctx, x, y, w, h, 0xFF2B2B2B);

            // name
            ctx.drawTextWithShadow(tr, setting.getDisplayName(), x + 8, y + 7, 0xFFDADADA);

            // right control area
            if (setting instanceof BoolSetting bs) {
                drawBool(ctx, tr, bs, mx, my);
            } else if (setting instanceof FloatSetting fs) {
                drawFloat(ctx, tr, fs, mx, my);
            } else {
                // unknown setting type
                ctx.drawTextWithShadow(tr, "N/A", x + w - 26, y + 7, 0xFFAAAAAA);
            }
        }

        private void drawBool(DrawContext ctx, net.minecraft.client.font.TextRenderer tr, BoolSetting bs, int mx, int my) {
            int swW = 56;
            int swH = 12;
            int swX = x + w - swW - 8;
            int swY = y + (h - swH) / 2;

            boolean hover = inRect(mx, my, swX, swY, swW, swH);

            int bg = bs.get() ? 0xFF1F8F4E : 0xFF3A3A3A;
            if (hover) bg = bs.get() ? 0xFF22A85B : 0xFF4A4A4A;

            ctx.fill(swX, swY, swX + swW, swY + swH, bg);
            ctx.drawCenteredTextWithShadow(tr, Text.literal(bs.get() ? "ON" : "OFF"), swX + swW / 2, swY + 2, 0xFFFFFFFF);
        }

        private void drawFloat(DrawContext ctx, net.minecraft.client.font.TextRenderer tr, FloatSetting fs, int mx, int my) {
            // [-] [ value ] [+]
            int btn = 14;
            int plusX = x + w - btn - 8;
            int minusX = plusX - btn - 60 - btn; // value box 60
            int valueX = minusX + btn;
            int y0 = y + (h - btn) / 2;

            // minus
            boolean mh = inRect(mx, my, minusX, y0, btn, btn);
            ctx.fill(minusX, y0, minusX + btn, y0 + btn, mh ? 0xFF2A2A2A : 0xFF202020);
            ctx.drawCenteredTextWithShadow(tr, Text.literal("-"), minusX + btn / 2, y0 + 3, 0xFFFFFFFF);

            // value
            ctx.fill(valueX, y0, valueX + 60, y0 + btn, 0xFF151515);
            border(ctx, valueX, y0, 60, btn, 0xFF2B2B2B);

            String v = formatFloat(fs.get());
            ctx.drawCenteredTextWithShadow(tr, Text.literal(v), valueX + 30, y0 + 3, 0xFFDADADA);

            // plus
            boolean ph = inRect(mx, my, plusX, y0, btn, btn);
            ctx.fill(plusX, y0, plusX + btn, y0 + btn, ph ? 0xFF2A2A2A : 0xFF202020);
            ctx.drawCenteredTextWithShadow(tr, Text.literal("+"), plusX + btn / 2, y0 + 3, 0xFFFFFFFF);
        }

        boolean mouseClicked(int mx, int my, int button) {
            // Bool
            if (setting instanceof BoolSetting bs) {
                int swW = 56;
                int swH = 12;
                int swX = x + w - swW - 8;
                int swY = y + (h - swH) / 2;

                if (inRect(mx, my, swX, swY, swW, swH)) {
                    bs.toggle();
                    return true;
                }
                return false;
            }

            // Float
            if (setting instanceof FloatSetting fs) {
                int btn = 14;
                int plusX = x + w - btn - 8;
                int minusX = plusX - btn - 60 - btn;
                int y0 = y + (h - btn) / 2;

                if (inRect(mx, my, minusX, y0, btn, btn)) {
                    fs.setClamped(fs.get() - step(fs));
                    return true;
                }
                if (inRect(mx, my, plusX, y0, btn, btn)) {
                    fs.setClamped(fs.get() + step(fs));
                    return true;
                }
            }

            return false;
        }

        private static float step(FloatSetting fs) {
            // Aralığa göre “mantıklı” step:
            float range = fs.max() - fs.min();
            if (range <= 1.0f) return 0.05f;
            if (range <= 3.0f) return 0.1f;
            if (range <= 12.0f) return 0.5f;
            return 1.0f;
        }

        private static String formatFloat(float f) {
            // 0.55 gibi değerlerde 2 basamak
            return (Math.round(f * 100f) / 100f) + "";
        }

        private static boolean inRect(int mx, int my, int x, int y, int w, int h) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        private static void border(DrawContext ctx, int x, int y, int w, int h, int argb) {
            ctx.fill(x, y, x + w, y + 1, argb);
            ctx.fill(x, y + h - 1, x + w, y + h, argb);
            ctx.fill(x, y, x + 1, y + h, argb);
            ctx.fill(x + w - 1, y, x + w, y + h, argb);
        }
    }

    // ---------------- utils ----------------

    private static boolean inRect(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void border(DrawContext ctx, int x, int y, int w, int h, int argb) {
        ctx.fill(x, y, x + w, y + 1, argb);
        ctx.fill(x, y + h - 1, x + w, y + h, argb);
        ctx.fill(x, y, x + 1, y + h, argb);
        ctx.fill(x + w - 1, y, x + w, y + h, argb);
    }
}
