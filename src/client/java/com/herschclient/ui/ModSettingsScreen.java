package com.herschclient.ui;

import com.herschclient.HerschClient;
import com.herschclient.core.config.ConfigManager;
import com.herschclient.core.hud.Widget;
import com.herschclient.core.module.Module;
import com.herschclient.core.settings.BoolSetting;
import com.herschclient.core.settings.FloatSetting;
import com.herschclient.core.settings.ModeSetting;
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

    // Layout Constants
    private final int SIDEBAR_WIDTH = 100;
    private final int TOP_BAR_HEIGHT = 30;
    private final int PADDING = 10;
    private final int GAP = 8;
    
    // Panel
    private int panelX, panelY, panelW, panelH;
    
    // Grid
    private int cardW = 160;
    private int cardH = 80;
    private int cols = 3;

    // Scroll
    private int scrollY = 0;
    private int contentHeight = 0;

    private enum Section {
        ALL("All Mods"), 
        HUD("HUD"), 
        MODULES("Modules");
        
        final String label;
        Section(String label) { this.label = label; }
    }
    
    private Section activeSection = Section.ALL;
    private final List<Card> cards = new ArrayList<>();

    private interface Card {
        void render(DrawContext ctx, int mouseX, int mouseY, int scrollY, net.minecraft.client.font.TextRenderer tr);
        boolean mouseClicked(int mx, int my, int scrollY, MinecraftClient mc);
        String getName();
    }

    public ModSettingsScreen(Screen parent) {
        super(Text.literal("Hersch Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Fullscreen-ish overlay with margins
        int margin = 30;
        panelW = this.width - margin * 2;
        panelH = this.height - margin * 2;
        panelX = margin;
        panelY = margin;

        rebuildCards();
    }

    private void rebuildCards() {
        cards.clear();
        List<Object> items = new ArrayList<>();

        if (activeSection == Section.HUD) {
            items.addAll(HerschClient.HUD.getWidgets());
        } else if (activeSection == Section.MODULES) {
            items.addAll(HerschClient.MODULES.all());
        } else {
            items.addAll(HerschClient.HUD.getWidgets());
            items.addAll(HerschClient.MODULES.all());
        }

        items.sort((a, b) -> {
            String an = (a instanceof Widget w) ? w.getName() : ((Module) a).getName();
            String bn = (b instanceof Widget w) ? w.getName() : ((Module) b).getName();
            return String.CASE_INSENSITIVE_ORDER.compare(an, bn);
        });

        // Calculate layout
        int contentX = panelX + SIDEBAR_WIDTH + PADDING;
        int contentW = panelW - SIDEBAR_WIDTH - PADDING * 2;
        
        cols = Math.max(1, contentW / (cardW + GAP));
        
        int col = 0;
        int row = 0;
        
        for (Object obj : items) {
            int x = contentX + col * (cardW + GAP);
            int y = (panelY + PADDING) + row * (cardH + GAP); // Relative to content start

            if (obj instanceof Widget w) {
                cards.add(new WidgetCard(x, y, cardW, cardH, w));
            } else if (obj instanceof Module m) {
                cards.add(new ModuleCard(x, y, cardW, cardH, m));
            }

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }
        
        int rows = (int) Math.ceil(items.size() / (double) cols);
        contentHeight = rows * (cardH + GAP) + PADDING * 2;
        scrollY = 0; // Reset scroll on tab change
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // 1. Dim Background
        this.renderBackground(ctx, mouseX, mouseY, delta);
        
        // 2. Main Panel BG
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF181818); // Dark Grey
        
        // 3. Sidebar BG
        ctx.fill(panelX, panelY, panelX + SIDEBAR_WIDTH, panelY + panelH, 0xFF141414); // Darker Grey
        
        // 4. Sidebar Separator
        ctx.fill(panelX + SIDEBAR_WIDTH, panelY, panelX + SIDEBAR_WIDTH + 1, panelY + panelH, 0xFF2A2A2A);

        // 5. Sidebar Items
        int itemY = panelY + 40;
        for (Section sec : Section.values()) {
            boolean active = (sec == activeSection);
            boolean hover = mouseX >= panelX && mouseX < panelX + SIDEBAR_WIDTH && mouseY >= itemY && mouseY < itemY + 24;
            
            int color = active ? 0xFFFFFFFF : (hover ? 0xFFE0E0E0 : 0xFF888888);
            
            if (active) {
                // Active Indicator (Blue Line)
                ctx.fill(panelX, itemY + 4, panelX + 2, itemY + 20, 0xFF3B72FF);
            }
            
            ctx.drawTextWithShadow(textRenderer, sec.label, panelX + 12, itemY + 8, color);
            itemY += 28;
        }

        // 6. Content Area Scissor
        int viewportX = panelX + SIDEBAR_WIDTH;
        int viewportY = panelY;
        int viewportW = panelW - SIDEBAR_WIDTH;
        int viewportH = panelH;
        
        ctx.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);
        
        // Render Cards with Scroll
        for (Card c : cards) {
            c.render(ctx, mouseX, mouseY, scrollY, textRenderer);
        }
        
        ctx.disableScissor();
        
        // 7. Header / Logo (Top Left of Sidebar)
        ctx.drawTextWithShadow(textRenderer, "HERSCH", panelX + 12, panelY + 12, 0xFF3B72FF);
        
        // 8. Close Button (Top Right)
        int closeSize = 20;
        int closeX = panelX + panelW - closeSize - 8;
        int closeY = panelY + 8;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + closeSize && mouseY >= closeY && mouseY <= closeY + closeSize;
        ctx.fill(closeX, closeY, closeX + closeSize, closeY + closeSize, closeHover ? 0xFF991111 : 0xFF2A2A2A);
        ctx.drawCenteredTextWithShadow(textRenderer, "X", closeX + closeSize / 2, closeY + 6, 0xFFFFFFFF);
        
        // 9. HUD Editor Button (Bottom Sidebar)
        int editBtnH = 24;
        int editBtnY = panelY + panelH - editBtnH - 12;
        boolean editHover = mouseX >= panelX + 8 && mouseX <= panelX + SIDEBAR_WIDTH - 8 && mouseY >= editBtnY && mouseY <= editBtnY + editBtnH;
        
        ctx.fill(panelX + 8, editBtnY, panelX + SIDEBAR_WIDTH - 8, editBtnY + editBtnH, editHover ? 0xFF3B72FF : 0xFF2A2A2A);
        ctx.drawCenteredTextWithShadow(textRenderer, "EDIT HUD", panelX + SIDEBAR_WIDTH / 2, editBtnY + 8, 0xFFFFFFFF);
        
        //super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Close
        int closeSize = 20;
        int closeX = panelX + panelW - closeSize - 8;
        int closeY = panelY + 8;
        if (mx >= closeX && mx <= closeX + closeSize && my >= closeY && my <= closeY + closeSize) {
            this.close();
            return true;
        }
        
        // Sidebar Navigation
        if (mx >= panelX && mx < panelX + SIDEBAR_WIDTH && my >= panelY + 40) {
            int itemY = panelY + 40;
            for (Section sec : Section.values()) {
                if (my >= itemY && my < itemY + 24) {
                    activeSection = sec;
                    rebuildCards();
                    return true;
                }
                itemY += 28;
            }
        }
        
        // Edit HUD Button
        int editBtnH = 24;
        int editBtnY = panelY + panelH - editBtnH - 12;
        if (mx >= panelX + 8 && mx <= panelX + SIDEBAR_WIDTH - 8 && my >= editBtnY && my <= editBtnY + editBtnH) {
             this.client.setScreen(new HudEditScreen(this));
             return true;
        }

        // Cards
        if (mx >= panelX + SIDEBAR_WIDTH) {
            for (Card c : cards) {
                if (c.mouseClicked(mx, my, scrollY, client)) return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
         if (mouseX >= panelX + SIDEBAR_WIDTH) {
             int maxScroll = Math.max(0, contentHeight - (panelH));
             scrollY = Math.max(0, Math.min(maxScroll, scrollY - (int)(verticalAmount * 20)));
             return true;
         }
         return false;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    // ================== CARDS ==================

    private static class WidgetCard implements Card {
        private final int x, y, w, h;
        private final Widget widget;

        WidgetCard(int x, int y, int w, int h, Widget widget) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.widget = widget;
        }

        @Override
        public String getName() { return widget.getName(); }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, int scrollY, net.minecraft.client.font.TextRenderer tr) {
            int ry = y - scrollY;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= ry && mouseY < ry + h;

            // Sharp BG
            ctx.fill(x, ry, x + w, ry + h, hover ? 0xFF242424 : 0xFF1E1E1E);
            // Sharp Outline
            int outlineColor = hover ? 0xFF3B72FF : 0xFF2A2A2A;
            drawSharpOutline(ctx, x, ry, w, h, outlineColor);

            // Icon Placeholder (Typography)
            String shortName = widget.getName().substring(0, Math.min(2, widget.getName().length())).toUpperCase();
            ctx.fill(x + 10, ry + 10, x + 40, ry + 40, 0xFF2A2A2A); // Icon Box
            
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x + 25, ry + 25, 0);
            ctx.getMatrices().scale(1.5f, 1.5f, 1f);
            ctx.drawCenteredTextWithShadow(tr, shortName, 0, -4, 0xFF555555);
            ctx.getMatrices().pop();

            // Name
            int toggleX = x + w - 30 - 10;
            int maxTextW = toggleX - (x + 50) - 5;
            String name = widget.getName();
            if (tr.getWidth(name) > maxTextW) {
                name = tr.trimToWidth(name, maxTextW - 6) + "...";
            }
            ctx.drawTextWithShadow(tr, name, x + 50, ry + 12, 0xFFFFFFFF);
            
            // Enabled Toggle (Switch style)
            int switchW = 30;
            int switchH = 14;
            int switchX = x + w - switchW - 10;
            int switchY = ry + 12;
            
            int switchBg = widget.isEnabled() ? 0xFF3B72FF : 0xFF353535;
            ctx.fill(switchX, switchY, switchX + switchW, switchY + switchH, switchBg);
            
            int knobOffset = widget.isEnabled() ? switchW - 12 : 2;
            ctx.fill(switchX + knobOffset, switchY + 2, switchX + knobOffset + 10, switchY + switchH - 2, 0xFFFFFFFF);
            
            // Options Button
            if (!widget.getSettings().isEmpty()) {
                int optH = 16;
                int optY = ry + h - optH - 8;
                boolean optHover = mouseX >= x + 8 && mouseX <= x + w - 8 && mouseY >= optY && mouseY <= optY + optH;
                
                ctx.fill(x + 8, optY, x + w - 8, optY + optH, optHover ? 0xFF353535 : 0xFF2A2A2A);
                ctx.drawCenteredTextWithShadow(tr, "SETTINGS", x + w / 2, optY + 4, 0xFFAAAAAA);
            }
        }

        @Override
        public boolean mouseClicked(int mx, int my, int scrollY, MinecraftClient mc) {
            int ry = y - scrollY;
            
            // Toggle
            int switchW = 30;
            int switchH = 14;
            int switchX = x + w - switchW - 10;
            int switchY = ry + 12;
            if (mx >= switchX && mx <= switchX + switchW && my >= switchY && my <= switchY + switchH) {
                widget.setEnabled(!widget.isEnabled());
                ConfigManager.save();
                return true;
            }
            
            // Options
            if (!widget.getSettings().isEmpty()) {
                int optH = 16;
                int optY = ry + h - optH - 8;
                if (mx >= x + 8 && mx <= x + w - 8 && my >= optY && my <= optY + optH) {
                    mc.setScreen(new SettingsDetailScreen(mc.currentScreen, widget.getName(), widget.getSettings()));
                    return true;
                }
            }
            
            return false;
        }
    }

    private static class ModuleCard implements Card {
        private final int x, y, w, h;
        private final Module module;

        ModuleCard(int x, int y, int w, int h, Module module) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.module = module;
        }

        @Override
        public String getName() { return module.getName(); }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, int scrollY, net.minecraft.client.font.TextRenderer tr) {
            int ry = y - scrollY;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= ry && mouseY < ry + h;

            // Sharp BG
            ctx.fill(x, ry, x + w, ry + h, hover ? 0xFF242424 : 0xFF1E1E1E);
            // Sharp Outline
            int outlineColor = hover ? 0xFF3B72FF : 0xFF2A2A2A;
            drawSharpOutline(ctx, x, ry, w, h, outlineColor);

            // Icon Placeholder
            String shortName = module.getName().substring(0, Math.min(2, module.getName().length())).toUpperCase();
            ctx.fill(x + 10, ry + 10, x + 40, ry + 40, 0xFF2A2A2A);
            
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x + 25, ry + 25, 0);
            ctx.getMatrices().scale(1.5f, 1.5f, 1f);
            ctx.drawCenteredTextWithShadow(tr, shortName, 0, -4, 0xFF555555);
            ctx.getMatrices().pop();

            // Name
            int toggleX = x + w - 30 - 10;
            int maxTextW = toggleX - (x + 50) - 5;
            String name = module.getName();
            if (tr.getWidth(name) > maxTextW) {
                name = tr.trimToWidth(name, maxTextW - 6) + "...";
            }
            ctx.drawTextWithShadow(tr, name, x + 50, ry + 12, 0xFFFFFFFF);
            
            // Category
            ctx.drawTextWithShadow(tr, module.getCategory().name(), x + 50, ry + 24, 0xFF888888);

            // Enabled Toggle
            int switchW = 30;
            int switchH = 14;
            int switchX = x + w - switchW - 10;
            int switchY = ry + 12;
            
            int switchBg = module.isEnabled() ? 0xFF3B72FF : 0xFF353535;
            ctx.fill(switchX, switchY, switchX + switchW, switchY + switchH, switchBg);
            
            int knobOffset = module.isEnabled() ? switchW - 12 : 2;
            ctx.fill(switchX + knobOffset, switchY + 2, switchX + knobOffset + 10, switchY + switchH - 2, 0xFFFFFFFF);

            // Options Button (NEW)
            if (!module.getSettings().isEmpty()) {
                int optH = 16;
                int optY = ry + h - optH - 8;
                boolean optHover = mouseX >= x + 8 && mouseX <= x + w - 8 && mouseY >= optY && mouseY <= optY + optH;
                
                ctx.fill(x + 8, optY, x + w - 8, optY + optH, optHover ? 0xFF353535 : 0xFF2A2A2A);
                ctx.drawCenteredTextWithShadow(tr, "SETTINGS", x + w / 2, optY + 4, 0xFFAAAAAA);
            }
        }

        @Override
        public boolean mouseClicked(int mx, int my, int scrollY, MinecraftClient mc) {
            int ry = y - scrollY;
            int switchW = 30;
            int switchH = 14;
            int switchX = x + w - switchW - 10;
            int switchY = ry + 12;
            
            if (mx >= switchX && mx <= switchX + switchW && my >= switchY && my <= switchY + switchH) {
                module.toggle();
                ConfigManager.save();
                return true;
            }

            // Options (NEW)
            if (!module.getSettings().isEmpty()) {
                int optH = 16;
                int optY = ry + h - optH - 8;
                if (mx >= x + 8 && mx <= x + w - 8 && my >= optY && my <= optY + optH) {
                    mc.setScreen(new SettingsDetailScreen(mc.currentScreen, module.getName(), module.getSettings()));
                    return true;
                }
            }
            return false;
        }
    }
    
    // ================== SETTINGS DETAIL SCREEN ==================
    
   private static final class SettingsDetailScreen extends Screen {
        private final Screen parent;
        private final String titleName;
        private final List<Setting<?>> settings;
        
        private int panelX, panelY, panelW, panelH;
        private String draggingKey = null;
        private int scrollY = 0;
        private int contentHeight = 0;

        protected SettingsDetailScreen(Screen parent, String titleName, List<Setting<?>> settings) {
            super(Text.literal("Options"));
            this.parent = parent;
            this.titleName = titleName;
            this.settings = settings;
        }

        @Override
        protected void init() {
            panelW = 320;
            panelH = 200;
            panelX = (this.width - panelW) / 2;
            panelY = (this.height - panelH) / 2;
            
            int h = 0;
            for (Setting<?> s : settings) {
                if (s instanceof BoolSetting || s instanceof ModeSetting) h += 24;
                else if (s instanceof FloatSetting) h += 28;
            }
            contentHeight = h;
        }

        @Override
        public void close() {
            this.client.setScreen(parent);
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            this.renderBackground(ctx, mouseX, mouseY, delta);
            
            ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF181818);
            drawSharpOutline(ctx, panelX, panelY, panelW, panelH, 0xFF2A2A2A);
            
            ctx.fill(panelX, panelY, panelX + panelW, panelY + 28, 0xFF202020);
            ctx.drawTextWithShadow(textRenderer, titleName.toUpperCase() + " SETTINGS", panelX + 12, panelY + 10, 0xFFFFFFFF);

            int closeX = panelX + panelW - 20;
            int closeY = panelY + 8;
            boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12;
            ctx.fill(closeX, closeY, closeX + 12, closeY + 12, hoverClose ? 0xFF991111 : 0xFF353535);
            ctx.drawCenteredTextWithShadow(textRenderer, "x", closeX + 6, closeY + 2, 0xFFFFFFFF);

            int contentY = panelY + 30;
            int contentH = panelH - 35;
            
            ctx.enableScissor(panelX, contentY, panelX + panelW, contentY + contentH);

            int y = (panelY + 40) - scrollY;
            int x = panelX + 12;
            int w = panelW - 24;

            for (Setting<?> s : settings) {
                int itemH = (s instanceof BoolSetting || s instanceof ModeSetting) ? 24 : 28;
                if (y + itemH < contentY || y > contentY + contentH) {
                    y += itemH;
                    continue;
                }

                if (s instanceof BoolSetting bs) {
                    ctx.drawTextWithShadow(textRenderer, bs.getDisplayName(), x, y + 4, 0xFFCCCCCC);
                    
                    int sw = 30;
                    int sx = x + w - sw;
                    int bg = bs.get() ? 0xFF3B72FF : 0xFF353535;
                    ctx.fill(sx, y, sx + sw, y + 16, bg);
                    
                    int k = bs.get() ? sw - 12 : 2;
                    ctx.fill(sx + k, y + 2, sx + k + 10, y + 14, 0xFFFFFFFF);
                    
                    y += 24;
                } else if (s instanceof FloatSetting fs) {
                    ctx.drawTextWithShadow(textRenderer, fs.getDisplayName(), x, y, 0xFFCCCCCC);
                    String val = String.format("%.1f", fs.get());
                    ctx.drawTextWithShadow(textRenderer, val, x + w - textRenderer.getWidth(val), y, 0xFF999999);
                    
                    y += 12;
                    ctx.fill(x, y, x + w, y + 6, 0xFF2A2A2A);
                    
                    float t = (fs.get() - fs.min()) / (fs.max() - fs.min());
                    int fw = (int) (w * t);
                    ctx.fill(x, y, x + fw, y + 6, 0xFF3B72FF);
                    
                    y += 16;
                } else if (s instanceof ModeSetting ms) {
                    ctx.drawTextWithShadow(textRenderer, ms.getDisplayName(), x, y + 4, 0xFFCCCCCC);
                    
                    String modeText = ms.get().toUpperCase();
                    int mw = textRenderer.getWidth(modeText) + 12;
                    int mx = x + w - mw;
                    
                    // Button BG
                    ctx.fill(mx, y, mx + mw, y + 16, 0xFF2A2A2A);
                    ctx.drawCenteredTextWithShadow(textRenderer, modeText, mx + mw / 2, y + 4, 0xFF3B72FF);
                    
                    y += 24;
                }
            }
            
            ctx.disableScissor();

            if (contentHeight > contentH) {
                int barX = panelX + panelW - 6;
                int barY = contentY;
                int barW = 2;
                int barH = contentH;
                
                float ratio = (float) contentH / contentHeight;
                int thumbH = Math.max(20, (int) (barH * ratio));
                int thumbY = barY + (int) ((float) scrollY / (contentHeight - contentH) * (barH - thumbH));
                
                ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF202020);
                ctx.fill(barX, thumbY, barX + barW, thumbY + thumbH, 0xFF505050);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int mx = (int) mouseX;
            int my = (int) mouseY;

            int closeX = panelX + panelW - 20;
            int closeY = panelY + 8;
            if (mx >= closeX && mx <= closeX + 12 && my >= closeY && my <= closeY + 12) {
                close();
                return true;
            }
            
            if (my < panelY + 30 || my > panelY + panelH - 5) return false;

            int y = (panelY + 40) - scrollY;
            int x = panelX + 12;
            int w = panelW - 24;
            
            for (Setting<?> s : settings) {
                if (s instanceof BoolSetting bs) {
                     int sw = 30;
                    int sx = x + w - sw;
                    if (mx >= sx && mx <= sx + sw && my >= y && my <= y + 16) {
                        bs.toggle();
                         ConfigManager.save();
                        return true;
                    }
                    y += 24;
                } else if (s instanceof FloatSetting fs) {
                    y += 12;
                    if (mx >= x && mx <= x + w && my >= y && my <= y + 6) {
                        draggingKey = fs.getKey();
                        updateSlider(fs, mx, x, w);
                        return true;
                    }
                    y += 16;
                } else if (s instanceof ModeSetting ms) {
                    // Click on mode button
                    // But actually, allow clicking anywhere on the right side or the whole row?
                    // Let's make the button clickable
                    String modeText = ms.get().toUpperCase();
                    int mw = textRenderer.getWidth(modeText) + 12;
                    int btnX = x + w - mw;
                    
                    if (mx >= btnX && mx <= btnX + mw && my >= y && my <= y + 16) {
                        ms.cycle();
                        ConfigManager.save();
                        return true;
                    }
                    y += 24;
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
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            if (draggingKey != null) {
                for (Setting<?> s : settings) {
                    if (s instanceof FloatSetting fs && fs.getKey().equals(draggingKey)) {
                         updateSlider(fs, (int)mouseX, panelX + 12, panelW - 24);
                         return true;
                    }
                }
            }
            return super.mouseDragged(mouseX, mouseY, button, dx, dy);
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            int contentH = panelH - 35;
            if (contentHeight > contentH) {
                int maxScroll = contentHeight - contentH + 20;
                scrollY = Math.max(0, Math.min(maxScroll, scrollY - (int)(verticalAmount * 20)));
                return true;
            }
            return false;
        }

        private void updateSlider(FloatSetting fs, int mx, int x, int w) {
            float t = (float)(mx - x) / w;
            if (t < 0) t = 0;
            if (t > 1) t = 1;
            float v = fs.min() + (fs.max() - fs.min()) * t;
            fs.setClamped(v);
            ConfigManager.save();
        }
        
        @Override
        public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
             ctx.fillGradient(0, 0, this.width, this.height, 0x80000000, 0x90000000);
        }
   }

    // ================== UTILS ==================

    private static void drawSharpOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);         // Top
        ctx.fill(x, y + h - 1, x + w, y + h, color); // Bottom
        ctx.fill(x, y, x + 1, y + h, color);         // Left
        ctx.fill(x + w - 1, y, x + w, y + h, color); // Right
    }
}
