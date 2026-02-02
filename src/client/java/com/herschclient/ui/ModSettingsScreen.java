package com.herschclient.ui;

import com.herschclient.HerschClient;
import com.herschclient.core.hud.Widget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class ModSettingsScreen extends Screen {

    private final Screen parent;

    public ModSettingsScreen(Screen parent) {
        super(Text.literal("Mod Ayarları"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 30;

        // HUD edit ekranı
        addDrawableChild(ButtonWidget.builder(
                Text.literal("HUD KONUM DÜZENLE"),
                b -> this.client.setScreen(new HudEditScreen(this))
        ).dimensions(cx - 102, y, 204, 20).build());

        y += 30;

        List<Widget> widgets = HerschClient.HUD.getWidgets();

        for (Widget w : widgets) {
            final Widget widget = w;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal(label(widget)),
                    b -> {
                        widget.setEnabled(!widget.isEnabled());
                        b.setMessage(Text.literal(label(widget)));
                    }
            ).dimensions(cx - 102, y, 204, 20).build());

            y += 24;
            if (y > this.height - 40) break; // taşarsa şimdilik kesiyoruz
        }

        // geri
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Geri"),
                b -> this.client.setScreen(parent)
        ).dimensions(cx - 102, this.height - 28, 204, 20).build());
    }

    private String label(Widget w) {
        return (w.isEnabled() ? "✅ " : "❌ ") + w.getName();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }
}
