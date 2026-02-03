package com.herschclient.ui;

import com.herschclient.HerschClient;
import com.herschclient.core.hud.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class ModSettingsScreen extends Screen {

    private final Screen parent;

    // Panel layout
    private int panelX, panelY, panelW, panelH;
    private int headerH = 26;
    private int tabsH = 22;
    private int pad = 10;

    // Grid layout
    private int cardW = 140;
    private int cardH = 92;
    private int gap = 10;
    private int cols = 3;

    // Scroll
    private int scrollY = 0;
    private int contentHeight = 0;

    // Tabs (şimdilik sadece ALL + HUD; ileride module kategorilerine bağlarız)
    private enum Tab { ALL, HUD }
    private Tab activeTab = Tab.ALL;

    private final List<WidgetCard> cards = new ArrayList<>();

    public ModSettingsScreen(Screen parent) {
        super(Text.literal("Mod Ayarları"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Panel boyutu: ekran küçülürse otomatik küçül
        panelW = Math.min(520, this.width - 40);
        panelH = Math.min(310, this.height - 40);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        // Kolon sayısını panel genişliğine göre ayarla
        cols = Math.max(2, (panelW - pad * 2) / (cardW + gap));
        cols = Math.min(cols, 4);

        rebuildCards();
    }

    private void rebuildCards() {
        cards.clear();

        List<Widget> widgets = HerschClient.HUD.getWidgets();

        // Tab filtre (şimdilik ALL/HUD aynı; ilerde WidgetCategory eklersen burada filtrelersin)
        List<Widget> filtered = new ArrayList<>(widgets);

        int contentX = panelX + pad;
        int contentY = panelY + headerH + tabsH + pad;
        int contentW = panelW - pad * 2;

        int col = 0;
        int row = 0;

        for (Widget w : filtered) {
            int x = contentX + col * (cardW + gap);
            int y = contentY + row * (cardH + gap);

            cards.add(new WidgetCard(x, y, cardW, cardH, w));

            col++;
            if (col >= cols || x + (cardW + gap) > contentX + contentW) {
                col = 0;
                row++;
            }
        }

        // içerik yüksekliği (scroll limit için)
        int rows = (int) Math.ceil(cards.size() / (double) cols);
        contentHeight = rows * (cardH + gap) - gap;
        scrollY = clamp(scrollY, 0, Math.max(0, contentHeight - getViewportH()));
    }

    private int getViewportY() {
        return panelY + headerH + tabsH + pad;
    }

    private int getViewportH() {
        return panelH - headerH - tabsH - pad - pad;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // Blur / arka plan istemiyoruz -> renderBackground ÇAĞIRMIYORUZ
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {

        // Panel arka plan (semi-transparent)
        fill(ctx, panelX, panelY, panelX + panelW, panelY + panelH, 0xB0121212);
        // İnce border
        drawBorder(ctx, panelX, panelY, panelW, panelH, 0xFF2B2B2B);

        // Header
        fill(ctx, panelX, panelY, panelX + panelW, panelY + headerH, 0xCC1A1A1A);
        ctx.drawTextWithShadow(textRenderer, "HERSCH CLIENT", panelX + 10, panelY + 9, 0xFFECECEC);

        // Sağ üst kapat butonu (X)
        int closeSize = 16;
        int closeX = panelX + panelW - closeSize - 8;
        int closeY = panelY + 5;
        boolean closeHover = inRect(mouseX, mouseY, closeX, closeY, closeSize, closeSize);
        fill(ctx, closeX, closeY, closeX + closeSize, closeY + closeSize, closeHover ? 0xFF3A3A3A : 0xFF2A2A2A);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("x"), closeX + closeSize / 2, closeY + 4, 0xFFFFFFFF);

        // Tabs
        int tabsY = panelY + headerH;
        fill(ctx, panelX, tabsY, panelX + panelW, tabsY + tabsH, 0xAA141414);

        drawTab(ctx, "ALL", Tab.ALL, panelX + 10, tabsY + 4, mouseX, mouseY);
        drawTab(ctx, "HUD", Tab.HUD, panelX + 54, tabsY + 4, mouseX, mouseY);

        // HUD Layout button (alt sol)
        int hudBtnW = 130;
        int hudBtnH = 16;
        int hudBtnX = panelX + 10;
        int hudBtnY = panelY + panelH - hudBtnH - 8;
        boolean hudHover = inRect(mouseX, mouseY, hudBtnX, hudBtnY, hudBtnW, hudBtnH);
        fill(ctx, hudBtnX, hudBtnY, hudBtnX + hudBtnW, hudBtnY + hudBtnH, hudHover ? 0xFF2E5CFF : 0xFF2747C9);
        ctx.drawCenteredTextWithShadow(
                textRenderer,
                "EDIT HUD LAYOUT",
                hudBtnX + hudBtnW / 2,
                hudBtnY + (hudBtnH - textRenderer.fontHeight) / 2,
                0xFFFFFF
        );

        // Scissor: sadece içerik alanında kartlar görünsün (scroll için şart)
        int vpX1 = panelX + pad;
        int vpY1 = getViewportY();
        int vpX2 = panelX + panelW - pad;
        int vpY2 = vpY1 + getViewportH();

        ctx.enableScissor(vpX1, vpY1, vpX2, vpY2);

        // Kartlar
        for (WidgetCard c : cards) {
            c.render(ctx, mouseX, mouseY, scrollY, this.textRenderer);
        }

        ctx.disableScissor();

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawTab(DrawContext ctx, String label, Tab tab, int x, int y, int mouseX, int mouseY) {
        int w = 36;
        int h = 14;
        boolean hover = inRect(mouseX, mouseY, x, y, w, h);

        int bg;
        if (activeTab == tab) bg = 0xFF3B72FF;
        else bg = hover ? 0xFF2C2C2C : 0xFF222222;

        fill(ctx, x, y, x + w, y + h, bg);
        ctx.drawTextWithShadow(textRenderer, label, x + 10, y + 3, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Close
        int closeSize = 16;
        int closeX = panelX + panelW - closeSize - 8;
        int closeY = panelY + 5;
        if (inRect(mx, my, closeX, closeY, closeSize, closeSize)) {
            close();
            return true;
        }

        // Tabs
        int tabsY = panelY + headerH;
        if (inRect(mx, my, panelX + 10, tabsY + 4, 36, 14)) {
            activeTab = Tab.ALL;
            rebuildCards();
            return true;
        }
        if (inRect(mx, my, panelX + 54, tabsY + 4, 36, 14)) {
            activeTab = Tab.HUD;
            rebuildCards();
            return true;
        }

        // HUD Layout
        int hudBtnW = 130;
        int hudBtnH = 16;
        int hudBtnX = panelX + 10;
        int hudBtnY = panelY + panelH - hudBtnH - 8;
        if (inRect(mx, my, hudBtnX, hudBtnY, hudBtnW, hudBtnH)) {
            this.client.setScreen(new HudEditScreen(this));
            return true;
        }

        // Cards click
        for (WidgetCard c : cards) {
            if (c.mouseClicked(mx, my, scrollY, this.client)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // sadece panel içindeyken scroll
        if (!inRect((int) mouseX, (int) mouseY, panelX, panelY, panelW, panelH)) return false;

        int maxScroll = Math.max(0, contentHeight - getViewportH());
        scrollY = clamp(scrollY - (int) (verticalAmount * 18), 0, maxScroll);
        return true;
    }

    // ---------- küçük util ----------

    private static void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int argb) {
        ctx.fill(x1, y1, x2, y2, argb);
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int argb) {
        // top
        ctx.fill(x, y, x + w, y + 1, argb);
        // bottom
        ctx.fill(x, y + h - 1, x + w, y + h, argb);
        // left
        ctx.fill(x, y, x + 1, y + h, argb);
        // right
        ctx.fill(x + w - 1, y, x + w, y + h, argb);
    }

    private static boolean inRect(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    // ---------- Kart ----------

    private static final class WidgetCard {
        private final int x, y, w, h;
        private final Widget widget;

        // alt bar
        private final int barH = 18;

        WidgetCard(int x, int y, int w, int h, Widget widget) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.widget = widget;
        }

        void render(DrawContext ctx, int mouseX, int mouseY, int scrollY, net.minecraft.client.font.TextRenderer tr) {
            int ry = y - scrollY;

            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= ry && mouseY < ry + h;

            // card bg
            ctx.fill(x, ry, x + w, ry + h, hover ? 0xCC202020 : 0xBB1C1C1C);
            // border
            // üst
            ctx.fill(x, y, x + w, y + 1, 0xFF2B2B2B);
            // alt
            ctx.fill(x, y + h - 1, x + w, y + h, 0xFF2B2B2B);
            // sol
            ctx.fill(x, y, x + 1, y + h, 0xFF2B2B2B);
            // sağ
            ctx.fill(x + w - 1, y, x + w, y + h, 0xFF2B2B2B);

            int iconSize = 48;
            int iconX = x + (w - iconSize) / 2;
            int iconY = ry + 34;

            var icon = widget.getIcon();
            if (icon != null) {
                int texSize = widget.getIconTextureSize(); // 64 gibi
                // Arka “plate” (isteğe bağlı, ikonu öne çıkarıyor)
                ctx.fill(iconX - 4, iconY - 4, iconX + iconSize + 4, iconY + iconSize + 4, 0xFF2A2A2A);

                // Texture çizimi
                ctx.drawTexture(
                        RenderLayer::getGuiTextured,
                        icon,
                        iconX,
                        iconY,
                        0f,
                        0f,
                        iconSize,
                        iconSize,
                        texSize,
                        texSize
                );

            } else {
                // Fallback: ikon yoksa boş kutu
                ctx.fill(iconX - 4, iconY - 4, iconX + iconSize + 4, iconY + iconSize + 4, 0xFF2A2A2A);
            }

            // Title
            String name = widget.getName();
            ctx.drawCenteredTextWithShadow(tr, Text.literal(name), x + w / 2, ry + 18, 0xFFEAEAEA);

            // Bottom bar
            int barY = ry + h - barH;
            ctx.fill(x, barY, x + w, barY + barH, 0xFF171717);

            // Options button (sol)
            int optX = x + 6;
            int optY = barY + 3;
            int optW = 70;
            int optH = 12;
            boolean optHover = inRect(mouseX, mouseY, optX, optY, optW, optH);
            ctx.fill(optX, optY, optX + optW, optY + optH, optHover ? 0xFF2A2A2A : 0xFF202020);
            ctx.drawTextWithShadow(tr, "OPTIONS", optX + 10, optY + 2, 0xFFDADADA);

            // Enabled toggle (sağ)
            int enW = 54;
            int enH = 12;
            int enX = x + w - enW - 6;
            int enY = optY;

            boolean enHover = inRect(mouseX, mouseY, enX, enY, enW, enH);

            int bg = widget.isEnabled() ? 0xFF1F8F4E : 0xFF3A3A3A;
            if (enHover) bg = widget.isEnabled() ? 0xFF22A85B : 0xFF4A4A4A;

            ctx.fill(enX, enY, enX + enW, enY + enH, bg);
            ctx.drawCenteredTextWithShadow(tr, Text.literal("ENABLED"), enX + enW / 2, enY + 2, 0xFFFFFFFF);
        }

        boolean mouseClicked(int mx, int my, int scrollY, MinecraftClient mc) {
            int ry = y - scrollY;

            // Options
            int barH = 18;
            int barY = ry + h - barH;
            int optX = x + 6;
            int optY = barY + 3;
            int optW = 70;
            int optH = 12;

            if (inRect(mx, my, optX, optY, optW, optH)) {
                if (!widget.getSettings().isEmpty()) {
                    mc.setScreen(new WidgetOptionsScreen(mc.currentScreen, widget));
                } else {
                    // settings yoksa fallback: HUD edit
                    mc.setScreen(new HudEditScreen(mc.currentScreen));
                }
                return true;
            }

            // Enabled
            int enW = 54;
            int enH = 12;
            int enX = x + w - enW - 6;
            int enY = optY;

            if (inRect(mx, my, enX, enY, enW, enH)) {
                widget.setEnabled(!widget.isEnabled());
                return true;
            }

            return false;
        }

        private static boolean inRect(int mx, int my, int x, int y, int w, int h) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Tamamen transparan: hiçbir şey çizme
    }
}
