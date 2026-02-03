package com.herschclient.ui;

import com.herschclient.HerschClient;
import com.herschclient.core.hud.Widget;
import com.herschclient.ui.widget.ModernButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.List;

public final class ModSettingsScreen extends Screen {

    private final Screen parent;

    // panel ölçüleri
    private int panelW = 320;
    private int panelH = 240;

    public ModSettingsScreen(Screen parent) {
        super(Text.literal("Mod Ayarları"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void init() {
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        int x = px + 20;
        int y = py + 40;
        int w = panelW - 40;
        int h = 22;

        // HUD edit ekranı
        addDrawableChild(new ModernButton(
                x, y, w, h,
                Text.literal("HUD KONUM DÜZENLE"),
                () -> this.client.setScreen(new HudEditScreen(this))
        ) {
            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }
        });

        y += 30;

        List<Widget> widgets = HerschClient.HUD.getWidgets();

        for (Widget widget : widgets) {
            Widget wdg = widget;

            ModernButton btn = new ModernButton(
                    x, y, w, h,
                    Text.literal(label(wdg)),
                    () -> {
                        wdg.setEnabled(!wdg.isEnabled());
                        // buton text güncelle
                        // PressableWidget'ta setMessage var:
                        // (ModernButton extends PressableWidget)
                    }
            ) {
                @Override
                protected void appendClickableNarrations(NarrationMessageBuilder builder) {

                }
            };

            // action içinde buton referansı gerek, bu yüzden küçük bir hack:
            ModernButton fixed = new ModernButton(
                    x, y, w, h,
                    Text.literal(label(wdg)),
                    () -> {
                    }
            ) {
                @Override
                protected void appendClickableNarrations(NarrationMessageBuilder builder) {

                }
            };
            // sonra action'ı değiştiremiyoruz; o yüzden en temizi: ayrı helper yazmak.
            // Pratik çözüm: aşağıdaki gibi anon inner kullan:
            ModernButton toggleBtn = new ModernButton(x, y, w, h, Text.literal(label(wdg)), () -> {}) {
                @Override
                protected void appendClickableNarrations(NarrationMessageBuilder builder) {

                }

                @Override
                public void onPress() {
                    wdg.setEnabled(!wdg.isEnabled());
                    this.setMessage(Text.literal(label(wdg)));
                }
            };

            addDrawableChild(toggleBtn);

            y += 26;
            if (y > py + panelH - 60) break;
        }

        // geri
        addDrawableChild(new ModernButton(
                x, py + panelH - 34, w, h,
                Text.literal("Geri"),
                () -> this.client.setScreen(parent)
        ) {
            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }
        });
    }

    private String label(Widget w) {
        return (w.isEnabled() ? "ON  " : "OFF ") + w.getName();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // ekran overlay (blur hissi)
        ctx.fill(0, 0, this.width, this.height, 0xAA000000);

        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        // panel arkaplanı
        ctx.fill(px, py, px + panelW, py + panelH, 0xDD141414);

        // panel border
        int border = 0xFF3A3A3A;
        ctx.fill(px, py, px + panelW, py + 1, border);
        ctx.fill(px, py + panelH - 1, px + panelW, py + panelH, border);
        ctx.fill(px, py, px + 1, py + panelH, border);
        ctx.fill(px + panelW - 1, py, px + panelW, py + panelH, border);

        // başlık
        ctx.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("MOD AYARLARI"),
                this.width / 2,
                py + 14,
                0xFFFFFF
        );

        super.render(ctx, mouseX, mouseY, delta);
    }
}
