package com.herschclient.ui;

import com.herschclient.HerschClient;
import com.herschclient.core.config.ConfigManager;
import com.herschclient.core.hud.Widget;
import com.herschclient.core.module.Module;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import com.herschclient.core.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ModSettingsScreen extends Screen {

    private final Screen parent;

    // Panel layout
    private int panelX, panelY, panelW, panelH;
    private final int headerH = 26;
    private final int tabsH = 22;
    private final int pad = 10;

    // Grid layout
    private final int cardW = 140;
    private final int cardH = 92;
    private final int gap = 10;
    private int cols = 3;

    // Scroll
    private int scrollY = 0;
    private int contentHeight = 0;

    private enum Tab { ALL, HUD, MODULES }
    private Tab activeTab = Tab.ALL;

    private interface Card {
        void render(DrawContext ctx, int mouseX, int mouseY, int scrollY, net.minecraft.client.font.TextRenderer tr);
        boolean mouseClicked(int mx, int my, int scrollY, MinecraftClient mc);
    }

    private final List<Card> cards = new ArrayList<>();

    public ModSettingsScreen(Screen parent) {
        super(Text.literal("Mod Ayarları"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = Math.min(520, this.width - 40);
        panelH = Math.min(310, this.height - 40);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        cols = Math.max(2, (panelW - pad * 2) / (cardW + gap));
        cols = Math.min(cols, 4);

        rebuildCards();
    }

    private void rebuildCards() {
        cards.clear();

        List<Object> items = new ArrayList<>();

        if (activeTab == Tab.HUD) {
            items.addAll(HerschClient.HUD.getWidgets());
        } else if (activeTab == Tab.MODULES) {
            items.addAll(HerschClient.MODULES.all());
        } else {
            // ALL: hem widget hem module
            items.addAll(HerschClient.HUD.getWidgets());
            items.addAll(HerschClient.MODULES.all());
        }

        // Stabil görünüm: widgets alfabetik, modules alfabetik
        items.sort((a, b) -> {
            String an = (a instanceof Widget w) ? w.getName() : ((Module) a).getName();
            String bn = (b instanceof Widget w) ? w.getName() : ((Module) b).getName();
            return String.CASE_INSENSITIVE_ORDER.compare(an, bn);
        });

        int contentX = panelX + pad;
        int contentY = panelY + headerH + tabsH + pad;
        int contentW = panelW - pad * 2;

        int col = 0;
        int row = 0;

        for (Object obj : items) {
            int x = contentX + col * (cardW + gap);
            int y = contentY + row * (cardH + gap);

            if (obj instanceof Widget w) {
                cards.add(new WidgetCard(x, y, cardW, cardH, w));
            } else if (obj instanceof Module m) {
                cards.add(new ModuleCard(x, y, cardW, cardH, m));
            }

            col++;
            if (col >= cols || x + (cardW + gap) > contentX + contentW) {
                col = 0;
                row++;
            }
        }

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

    // Blur yok -> renderBackground çağırmıyoruz
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Panel
        fill(ctx, panelX, panelY, panelX + panelW, panelY + panelH, 0xB0121212);
        drawBorder(ctx, panelX, panelY, panelW, panelH, 0xFF2B2B2B);

        // Header
        fill(ctx, panelX, panelY, panelX + panelW, panelY + headerH, 0xCC1A1A1A);
        ctx.drawTextWithShadow(textRenderer, "HERSCH CLIENT", panelX + 10, panelY + 9, 0xFFECECEC);

        // Close button
        int closeSize = 16;
        int closeX = panelX + panelW - closeSize - 8;
        int closeY = panelY + 5;
        boolean closeHover = inRect(mouseX, mouseY, closeX, closeY, closeSize, closeSize);
        fill(ctx, closeX, closeY, closeX + closeSize, closeY + closeSize, closeHover ? 0xFF3A3A3A : 0xFF2A2A2A);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("x"), closeX + closeSize / 2, closeY + 4, 0xFFFFFFFF);

        // Tabs bar
        int tabsY = panelY + headerH;
        fill(ctx, panelX, tabsY, panelX + panelW, tabsY + tabsH, 0xAA141414);

        drawTab(ctx, "ALL", Tab.ALL, panelX + 10, tabsY + 4, 36, mouseX, mouseY);
        drawTab(ctx, "HUD", Tab.HUD, panelX + 54, tabsY + 4, 36, mouseX, mouseY);
        drawTab(ctx, "MODULES", Tab.MODULES, panelX + 98, tabsY + 4, 62, mouseX, mouseY);

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

        // Scissor (scroll)
        int vpX1 = panelX + pad;
        int vpY1 = getViewportY();
        int vpX2 = panelX + panelW - pad;
        int vpY2 = vpY1 + getViewportH();

        ctx.enableScissor(vpX1, vpY1, vpX2, vpY2);

        for (Card c : cards) {
            c.render(ctx, mouseX, mouseY, scrollY, this.textRenderer);
        }

        ctx.disableScissor();

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawTab(DrawContext ctx, String label, Tab tab, int x, int y, int w, int mouseX, int mouseY) {
        int h = 14;
        boolean hover = inRect(mouseX, mouseY, x, y, w, h);

        int bg;
        if (activeTab == tab) bg = 0xFF3B72FF;
        else bg = hover ? 0xFF2C2C2C : 0xFF222222;

        fill(ctx, x, y, x + w, y + h, bg);
        ctx.drawTextWithShadow(textRenderer, label, x + 8, y + 3, 0xFFFFFFFF);
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
        if (inRect(mx, my, panelX + 98, tabsY + 4, 62, 14)) {
            activeTab = Tab.MODULES;
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

        // Cards
        for (Card c : cards) {
            if (c.mouseClicked(mx, my, scrollY, this.client)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!inRect((int) mouseX, (int) mouseY, panelX, panelY, panelW, panelH)) return false;

        int maxScroll = Math.max(0, contentHeight - getViewportH());
        scrollY = clamp(scrollY - (int) (verticalAmount * 18), 0, maxScroll);
        return true;
    }

    // ---------------- util ----------------

    private static void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int argb) {
        ctx.fill(x1, y1, x2, y2, argb);
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int argb) {
        ctx.fill(x, y, x + w, y + 1, argb);
        ctx.fill(x, y + h - 1, x + w, y + h, argb);
        ctx.fill(x, y, x + 1, y + h, argb);
        ctx.fill(x + w - 1, y, x + w, y + h, argb);
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static boolean inRect(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    // ---------------- Widget Card ----------------

    private static final class WidgetCard implements Card {
        private final int x, y, w, h;
        private final Widget widget;
        private final int barH = 18;

        WidgetCard(int x, int y, int w, int h, Widget widget) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.widget = widget;
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, int scrollY, net.minecraft.client.font.TextRenderer tr) {
            int ry = y - scrollY;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= ry && mouseY < ry + h;

            // bg
            ctx.fill(x, ry, x + w, ry + h, hover ? 0xCC202020 : 0xBB1C1C1C);

            // border (ry ile)
            ctx.fill(x, ry, x + w, ry + 1, 0xFF2B2B2B);
            ctx.fill(x, ry + h - 1, x + w, ry + h, 0xFF2B2B2B);
            ctx.fill(x, ry, x + 1, ry + h, 0xFF2B2B2B);
            ctx.fill(x + w - 1, ry, x + w, ry + h, 0xFF2B2B2B);

            // Title
            ctx.drawCenteredTextWithShadow(tr, Text.literal(widget.getName()), x + w / 2, ry + 18, 0xFFEAEAEA);

            // Icon
            int iconSize = 48;
            int iconX = x + (w - iconSize) / 2;
            int iconY = ry + 34;

            var icon = widget.getIcon();
            if (icon != null) {
                int texSize = widget.getIconTextureSize();
                ctx.fill(iconX - 4, iconY - 4, iconX + iconSize + 4, iconY + iconSize + 4, 0xFF2A2A2A);

                ctx.drawTexture(
                        RenderLayer::getGuiTextured,
                        icon,
                        iconX,
                        iconY,
                        0f, 0f,
                        iconSize, iconSize,
                        texSize, texSize
                );
            } else {
                ctx.fill(iconX - 4, iconY - 4, iconX + iconSize + 4, iconY + iconSize + 4, 0xFF2A2A2A);
            }

            // Bottom bar
            int barY = ry + h - barH;
            ctx.fill(x, barY, x + w, barY + barH, 0xFF171717);

            // Options (sol)
            int optX = x + 6;
            int optY = barY + 3;
            int optW = 70;
            int optH = 12;
            boolean optHover = inRect(mouseX, mouseY, optX, optY, optW, optH);
            ctx.fill(optX, optY, optX + optW, optY + optH, optHover ? 0xFF2A2A2A : 0xFF202020);
            ctx.drawTextWithShadow(tr, "OPTIONS", optX + 10, optY + 2, 0xFFDADADA);

            // Enabled (sağ)
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

        @Override
        public boolean mouseClicked(int mx, int my, int scrollY, MinecraftClient mc) {
            int ry = y - scrollY;

            int barY = ry + h - barH;
            int optX = x + 6;
            int optY = barY + 3;
            int optW = 70;
            int optH = 12;

            if (inRect(mx, my, optX, optY, optW, optH)) {
                mc.setScreen(new WidgetOptionsScreen(mc.currentScreen, widget));
                return true;
            }

            int enW = 54;
            int enH = 12;
            int enX = x + w - enW - 6;
            int enY = optY;

            if (inRect(mx, my, enX, enY, enW, enH)) {
                widget.setEnabled(!widget.isEnabled());
                ConfigManager.save();
                return true;
            }

            return false;
        }
    }

    // ---------------- Module Card ----------------

    private static final class ModuleCard implements Card {
        private final int x, y, w, h;
        private final Module module;
        private final int barH = 18;

        ModuleCard(int x, int y, int w, int h, Module module) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.module = module;
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, int scrollY, net.minecraft.client.font.TextRenderer tr) {
            int ry = y - scrollY;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= ry && mouseY < ry + h;

            ctx.fill(x, ry, x + w, ry + h, hover ? 0xCC202020 : 0xBB1C1C1C);

            // border (ry)
            ctx.fill(x, ry, x + w, ry + 1, 0xFF2B2B2B);
            ctx.fill(x, ry + h - 1, x + w, ry + h, 0xFF2B2B2B);
            ctx.fill(x, ry, x + 1, ry + h, 0xFF2B2B2B);
            ctx.fill(x + w - 1, ry, x + w, ry + h, 0xFF2B2B2B);

            ctx.drawCenteredTextWithShadow(tr, Text.literal(module.getName()), x + w / 2, ry + 18, 0xFFEAEAEA);
            ctx.drawCenteredTextWithShadow(tr, Text.literal(module.getCategory().name()), x + w / 2, ry + 32, 0xFFB0B0B0);

            // Bottom bar
            int barY = ry + h - barH;
            ctx.fill(x, barY, x + w, barY + barH, 0xFF171717);

            // Options placeholder (sol)
            int optX = x + 6;
            int optY = barY + 3;
            int optW = 70;
            int optH = 12;
            boolean optHover = inRect(mouseX, mouseY, optX, optY, optW, optH);
            ctx.fill(optX, optY, optX + optW, optY + optH, optHover ? 0xFF2A2A2A : 0xFF202020);
            ctx.drawTextWithShadow(tr, "OPTIONS", optX + 10, optY + 2, 0xFFDADADA);

            // Enabled (sağ)
            int enW = 54;
            int enH = 12;
            int enX = x + w - enW - 6;
            int enY = optY;

            boolean enHover = inRect(mouseX, mouseY, enX, enY, enW, enH);

            int bg = module.isEnabled() ? 0xFF1F8F4E : 0xFF3A3A3A;
            if (enHover) bg = module.isEnabled() ? 0xFF22A85B : 0xFF4A4A4A;

            ctx.fill(enX, enY, enX + enW, enY + enH, bg);
            ctx.drawCenteredTextWithShadow(tr, Text.literal("ENABLED"), enX + enW / 2, enY + 2, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(int mx, int my, int scrollY, MinecraftClient mc) {
            int ry = y - scrollY;

            int barY = ry + h - barH;

            // options (şimdilik bir şey yapmıyor)
            int optX = x + 6;
            int optY = barY + 3;
            int optW = 70;
            int optH = 12;
            if (inRect(mx, my, optX, optY, optW, optH)) {
                return true;
            }

            int enW = 54;
            int enH = 12;
            int enX = x + w - enW - 6;
            int enY = optY;

            if (inRect(mx, my, enX, enY, enW, enH)) {
                module.toggle();
                ConfigManager.save();
                return true;
            }

            return false;
        }
    }

    // ---------------- Options Screen (Widget settings) ----------------

    private static final class WidgetOptionsScreen extends Screen {

        private final Screen parent;
        private final Widget widget;

        // panel
        private int panelX, panelY, panelW, panelH;

        // drag state (float slider)
        private String draggingKey = null;

        protected WidgetOptionsScreen(Screen parent, Widget widget) {
            super(Text.literal("Options"));
            this.parent = parent;
            this.widget = widget;
        }

        @Override
        protected void init() {
            panelW = Math.min(360, this.width - 40);
            panelH = Math.min(240, this.height - 40);
            panelX = (this.width - panelW) / 2;
            panelY = (this.height - panelH) / 2;
        }

        @Override
        public void close() {
            this.client.setScreen(parent);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            // panel
            ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xB0121212);
            ctx.fill(panelX, panelY, panelX + panelW, panelY + 24, 0xCC1A1A1A);
            drawBorder(ctx, panelX, panelY, panelW, panelH, 0xFF2B2B2B);

            // title
            ctx.drawTextWithShadow(textRenderer, widget.getName() + " - OPTIONS", panelX + 10, panelY + 8, 0xFFECECEC);

            // close
            int closeSize = 16;
            int closeX = panelX + panelW - closeSize - 8;
            int closeY = panelY + 4;
            boolean closeHover = inRect(mouseX, mouseY, closeX, closeY, closeSize, closeSize);
            ctx.fill(closeX, closeY, closeX + closeSize, closeY + closeSize, closeHover ? 0xFF3A3A3A : 0xFF2A2A2A);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("x"), closeX + closeSize / 2, closeY + 4, 0xFFFFFFFF);

            int x = panelX + 12;
            int y = panelY + 34;

            // settings
            for (Setting<?> s : widget.getSettings()) {
                if (y > panelY + panelH - 30) break;

                if (s instanceof BoolSetting bs) {
                    // row
                    ctx.drawTextWithShadow(textRenderer, bs.getDisplayName(), x, y + 2, 0xFFDADADA);

                    int bw = 80;
                    int bh = 14;
                    int bx = panelX + panelW - bw - 12;
                    int by = y;

                    boolean hover = inRect(mouseX, mouseY, bx, by, bw, bh);
                    int bg = bs.get() ? 0xFF1F8F4E : 0xFF3A3A3A;
                    if (hover) bg = bs.get() ? 0xFF22A85B : 0xFF4A4A4A;

                    ctx.fill(bx, by, bx + bw, by + bh, bg);
                    ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(bs.get() ? "ON" : "OFF"), bx + bw / 2, by + 3, 0xFFFFFFFF);

                    y += 20;
                }
                else if (s instanceof FloatSetting fs) {
                    // label
                    ctx.drawTextWithShadow(textRenderer, fs.getDisplayName(), x, y + 2, 0xFFDADADA);

                    // slider bar
                    int sw = 160;
                    int sh = 10;
                    int sx = panelX + panelW - sw - 12;
                    int sy = y + 2;

                    // background bar
                    boolean hover = inRect(mouseX, mouseY, sx, sy, sw, sh);
                    ctx.fill(sx, sy, sx + sw, sy + sh, hover ? 0xFF2A2A2A : 0xFF202020);

                    float t = (fs.get() - fs.min()) / (fs.max() - fs.min());
                    if (t < 0) t = 0;
                    if (t > 1) t = 1;

                    int fillW = (int) (sw * t);
                    ctx.fill(sx, sy, sx + fillW, sy + sh, 0xFF3B72FF);

                    // knob
                    int kx = sx + fillW - 2;
                    ctx.fill(kx, sy - 2, kx + 4, sy + sh + 2, 0xFFECECEC);

                    // value text
                    String val = String.format("%.2f", fs.get());
                    ctx.drawTextWithShadow(textRenderer, val, sx + sw + 6, y + 1, 0xFFBEBEBE);

                    y += 20;
                }
            }

            super.render(ctx, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int mx = (int) mouseX;
            int my = (int) mouseY;

            // close
            int closeSize = 16;
            int closeX = panelX + panelW - closeSize - 8;
            int closeY = panelY + 4;
            if (inRect(mx, my, closeX, closeY, closeSize, closeSize)) {
                close();
                return true;
            }

            int x = panelX + 12;
            int y = panelY + 34;

            for (Setting<?> s : widget.getSettings()) {
                if (y > panelY + panelH - 30) break;

                if (s instanceof BoolSetting bs) {
                    int bw = 80;
                    int bh = 14;
                    int bx = panelX + panelW - bw - 12;
                    int by = y;

                    if (inRect(mx, my, bx, by, bw, bh)) {
                        bs.toggle();
                        ConfigManager.save();
                        return true;
                    }
                    y += 20;
                }
                else if (s instanceof FloatSetting fs) {
                    int sw = 160;
                    int sh = 10;
                    int sx = panelX + panelW - sw - 12;
                    int sy = y + 2;

                    if (inRect(mx, my, sx, sy - 4, sw, sh + 8)) {
                        draggingKey = fs.getKey();
                        setFloatFromMouse(fs, mx, sx, sw);
                        ConfigManager.save();
                        return true;
                    }
                    y += 20;
                }
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            draggingKey = null;
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (draggingKey == null) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

            int mx = (int) mouseX;

            // hangi setting drag ediliyor bul
            FloatSetting target = null;
            for (Setting<?> s : widget.getSettings()) {
                if (s instanceof FloatSetting fs && fs.getKey().equals(draggingKey)) {
                    target = fs;
                    break;
                }
            }
            if (target == null) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

            int sw = 160;
            int sx = panelX + panelW - sw - 12;

            setFloatFromMouse(target, mx, sx, sw);
            ConfigManager.save();
            return true;
        }

        private void setFloatFromMouse(FloatSetting fs, int mx, int sx, int sw) {
            float t = (mx - sx) / (float) sw;
            if (t < 0) t = 0;
            if (t > 1) t = 1;
            float v = fs.min() + (fs.max() - fs.min()) * t;
            fs.setClamped(v);
        }

        @Override
        public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
            // tamamen transparan
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Tamamen transparan: hiçbir şey çizme
    }
}
