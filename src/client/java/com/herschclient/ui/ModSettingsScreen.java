package com.herschclient.ui;

import com.herschclient.HerschClient;
import com.herschclient.core.hud.Widget;
import com.herschclient.ui.lunar.LunarButton;
import com.herschclient.ui.lunar.LunarColors;
import com.herschclient.ui.lunar.ModuleCardWidget;
import com.herschclient.ui.lunar.UiDraw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class ModSettingsScreen extends Screen {

    private final Screen parent;

    // panel geometry (draggable)
    private int panelW = 560;
    private int panelH = 320;
    private int panelX;
    private int panelY;

    private boolean dragging = false;
    private int dragOffX, dragOffY;

    private final List<ModuleCardWidget> cards = new ArrayList<>();

    private Integer prevMenuBlur = null;

    public ModSettingsScreen(Screen parent) {
        super(Text.literal("")); // kendi header çiziyoruz
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.client != null && this.client.gameRenderer != null) {
            this.client.gameRenderer.clearPostProcessor();
        }

        if (this.client != null) {
            var opt = this.client.options.getMenuBackgroundBlurriness();
            this.prevMenuBlur = opt.getValue();
            opt.setValue(0); // blur = 0
        }

        cards.clear();
        this.clearChildren();

        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        // Close button (top-right)
        addDrawableChild(new LunarButton(
                panelX + panelW - 26, panelY + 8,
                18, 18,
                Text.literal("x"),
                () -> this.client.setScreen(parent),
                6, 0xFF2B2B2B, 0xFF3A3A3A
        ) {
            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }
        });

        // Edit HUD Layout (bottom-left inside panel)
        addDrawableChild(new LunarButton(
                panelX + 14, panelY + panelH - 26,
                140, 16,
                Text.literal("EDIT HUD LAYOUT"),
                () -> this.client.setScreen(new HudEditScreen(this)),
                6, 0xFF2B2B2B, 0xFF3A3A3A
        ) {
            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }
        });


        // Build cards (HUD widgets)
        List<Widget> widgets = HerschClient.HUD.getWidgets();

        // grid area
        int gridX = panelX + 170;
        int gridY = panelY + 64;
        int cardW = 160;
        int cardH = 110;
        int gap = 12;

        int col = 0, row = 0;
        int maxCols = 2; // panelW 560 → orta alan 560-170-14 = 376 → 2 kart güzel durur

        for (Widget w : widgets) {
            int x = gridX + col * (cardW + gap);
            int y = gridY + row * (cardH + gap);

            ModuleCardWidget card = new ModuleCardWidget(
                    x, y, cardW, cardH, w,
                    () -> {
                    } // şimdilik boş (options ekranını sonra bağlarız)
            ) {
                @Override
                protected void appendClickableNarrations(NarrationMessageBuilder builder) {

                }
            };

            cards.add(card);
            addDrawableChild(card);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }

    @Override
    public void removed() {
        if (this.client != null && prevMenuBlur != null) {
            this.client.options.getMenuBackgroundBlurriness().setValue(prevMenuBlur);
        }
        super.removed();
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Tamamen transparan: hiçbir şey çizme
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.gameRenderer != null) {
            this.client.gameRenderer.clearPostProcessor();
        }

        // panel
        UiDraw.roundedRect(ctx, panelX, panelY, panelW, panelH, 10, LunarColors.PANEL_BG);

        // header bar
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 34, LunarColors.HEADER_BG);

        // sidebar
        ctx.fill(panelX, panelY + 34, panelX + 160, panelY + panelH, LunarColors.SIDEBAR_BG);

        // Header texts (logo placeholder + tabs)
        ctx.drawTextWithShadow(textRenderer, Text.literal("HERSCH CLIENT"), panelX + 14, panelY + 12, LunarColors.TEXT_MAIN);

        // Tabs
        drawTab(ctx, "MODS", panelX + 180, panelY + 10, true);
        drawTab(ctx, "SETTINGS", panelX + 236, panelY + 10, false);
        drawTab(ctx, "WAYPOINTS", panelX + 320, panelY + 10, false);

        // Sidebar section
        ctx.drawTextWithShadow(textRenderer, Text.literal("Default"), panelX + 14, panelY + 46, LunarColors.TEXT_MAIN);
        ctx.drawTextWithShadow(textRenderer, Text.literal("HUD"), panelX + 14, panelY + 70, LunarColors.TEXT_DIM);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Server"), panelX + 14, panelY + 90, LunarColors.TEXT_DIM);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Mechanic"), panelX + 14, panelY + 110, LunarColors.TEXT_DIM);

        // Chips + search bar (top middle area)
        int chipsX = panelX + 170;
        int chipsY = panelY + 40;
        drawChip(ctx, "ALL", chipsX, chipsY, true);
        drawChip(ctx, "NEW", chipsX + 44, chipsY, false);
        drawChip(ctx, "HUD", chipsX + 88, chipsY, true);
        drawChip(ctx, "SERVER", chipsX + 132, chipsY, false);

        // search
        UiDraw.roundedRect(ctx, panelX + panelW - 190, panelY + 38, 160, 18, 7, 0xFF2A2A2A);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Search..."), panelX + panelW - 180, panelY + 43, LunarColors.TEXT_DIM);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawTab(DrawContext ctx, String text, int x, int y, boolean active) {
        int w = textRenderer.getWidth(text) + 14;
        int bg = active ? 0xFF2E2E2E : 0xFF232323;
        UiDraw.roundedRect(ctx, x, y, w, 16, 6, bg);
        ctx.drawTextWithShadow(textRenderer, Text.literal(text), x + 7, y + 4, active ? LunarColors.TEXT_MAIN : LunarColors.TEXT_DIM);
    }

    private void drawChip(DrawContext ctx, String text, int x, int y, boolean active) {
        int w = textRenderer.getWidth(text) + 14;
        int bg = active ? LunarColors.CHIP_BG_ON : LunarColors.CHIP_BG;
        UiDraw.roundedRect(ctx, x, y, w, 16, 6, bg);
        ctx.drawTextWithShadow(textRenderer, Text.literal(text), x + 7, y + 4, LunarColors.TEXT_MAIN);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // dragging: header bar alanı
        if (button == 0) {
            boolean onHeader = mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + 34;
            if (onHeader) {
                dragging = true;
                dragOffX = (int) mouseX - panelX;
                dragOffY = (int) mouseY - panelY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            panelX = (int) mouseX - dragOffX;
            panelY = (int) mouseY - dragOffY;
            // çocuk widgetlar panelin coordinate’lerine bağlı olduğu için: yeniden init
            this.init(this.client, this.width, this.height);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public boolean shouldRenderInGame() {
        return true;
    }
}
